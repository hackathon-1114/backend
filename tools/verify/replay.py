"""Replays pose-test.html's tracking rules over the dataset's MediaPipe keypoint CSVs.

The rules below mirror the JavaScript in src/main/resources/static/pose-test.html line for line,
so a result here is a result for the same logic the browser runs. Input is the folder of filtered
keypoint CSVs given as the first argument.
"""
import csv
import io
import json
import math
import os
import sys


REP_START, REP_END, REP_COUNTED = 0.90, 0.93, 0.80
MIN_VISIBILITY, FRONT_VIEW_RATIO = 0.5, 0.45
SHALLOW_THIGH, LEAN_LIMIT, KNEE_IN_RATIO, SHIFT_LIMIT = 15, 50, 0.80, 0.25
BEND_LEAN, BEND_THIGH = 65, 30


def measure(lm, w, h):
    p = lambda i: (lm[i][0] * w, lm[i][1] * h, lm[i][2])
    L = dict(shoulder=p(11), hip=p(23), knee=p(25), ankle=p(27))
    R = dict(shoulder=p(12), hip=p(24), knee=p(26), ankle=p(28))
    vis = lambda s: min(s["hip"][2], s["knee"][2], s["ankle"][2])
    if max(vis(L), vis(R)) < MIN_VISIBILITY:
        return None
    mid = lambda a, b: ((a[0] + b[0]) / 2, (a[1] + b[1]) / 2)
    hip, ankle, shoulder = mid(L["hip"], R["hip"]), mid(L["ankle"], R["ankle"]), mid(L["shoulder"], R["shoulder"])
    torso = math.hypot(shoulder[0] - hip[0], shoulder[1] - hip[1]) or 1
    front = abs(L["shoulder"][0] - R["shoulder"][0]) / torso > FRONT_VIEW_RATIO
    near = L if vis(L) >= vis(R) else R
    out = dict(
        front=front,
        legHeight=ankle[1] - hip[1],
        torso=torso,
        lean=math.degrees(math.atan2(abs(shoulder[0] - hip[0]), abs(hip[1] - shoulder[1]))),
        thigh=math.degrees(math.atan2(near["knee"][1] - near["hip"][1], abs(near["knee"][0] - near["hip"][0]))),
        kneeRatio=None, shift=None)
    ankle_gap = abs(L["ankle"][0] - R["ankle"][0])
    if front and ankle_gap > torso * 0.15:
        out["kneeRatio"] = abs(L["knee"][0] - R["knee"][0]) / ankle_gap
        out["shift"] = (hip[0] - ankle[0]) / ankle_gap
    return out


def findings(rep):
    f = []
    if rep["view"] == "side":
        if rep["thighAngle"] > SHALLOW_THIGH: f.append("깊이 부족")
        if rep["maxLean"] > LEAN_LIMIT: f.append("상체 숙임 큼")
    else:
        if rep["kneeRatio"] is not None and rep["kneeRatio"] < KNEE_IN_RATIO: f.append("무릎 안쪽 모임")
        if rep["shift"] is not None and abs(rep["shift"]) > SHIFT_LIMIT: f.append("좌우 쏠림")
    return f


def replay(frames, w, h):
    t = dict(standing=0, standingTorso=0, smooth=None, phase="up", min=1, bottom=None, maxLean=0, minTorso=1, start=0, views=[])
    reps, partials, ignored, detected = [], [], [], 0
    for now, lm in frames:
        m = measure(lm, w, h)
        if m is None:
            continue
        detected += 1
        t["standing"] = max(t["standing"] * 0.999, m["legHeight"])
        t["standingTorso"] = max(t["standingTorso"] * 0.999, m["torso"])
        torso_ratio = m["torso"] / t["standingTorso"]
        ratio = m["legHeight"] / t["standing"]
        t["smooth"] = ratio if t["smooth"] is None else t["smooth"] * 0.6 + ratio * 0.4
        if t["phase"] == "up":
            if t["smooth"] < REP_START:
                t.update(phase="moving", min=t["smooth"], bottom=m, maxLean=m["lean"], minTorso=torso_ratio, start=now, views=[], bottomMs=now)
            continue
        t["views"].append(m["front"])
        t["maxLean"] = max(t["maxLean"], m["lean"])
        t["minTorso"] = min(t["minTorso"], torso_ratio)
        if t["smooth"] < t["min"]:
            t["min"], t["bottom"], t["bottomMs"] = t["smooth"], m, now
        if t["smooth"] > REP_END:
            front = sum(t["views"]) > len(t["views"]) / 2
            b = t["bottom"]
            rep = dict(view="front" if front else "side", depth=round(t["min"], 3), durationMs=round(now - t["start"]),
                       maxLean=round(t["maxLean"], 1), thighAngle=round(b["thigh"], 1),
                       kneeRatio=None if b["kneeRatio"] is None else round(b["kneeRatio"], 3),
                       shift=None if b["shift"] is None else round(b["shift"], 3), minTorso=round(t["minTorso"], 3), bottomMs=round(t["bottomMs"]))
            rep["findings"] = findings(rep)
            bend = (not front) and rep["maxLean"] > BEND_LEAN and rep["thighAngle"] > BEND_THIGH
            (ignored if bend else reps if t["min"] <= REP_COUNTED else partials).append(rep)
            t["phase"], t["min"] = "up", 1
    return reps, partials, ignored, detected


def load(path):
    frames, cur, cur_id, w, h = [], None, None, 0, 0
    with open(path, newline="") as fh:
        for row in csv.DictReader(fh):
            if row["frame"] != cur_id:
                cur_id = row["frame"]
                cur = {}
                frames.append((float(row["timestamp_ms"]), cur))
                w, h = int(row["frame_width"]), int(row["frame_height"])
            if row["x"]:
                cur[int(row["landmark_id"])] = (float(row["x"]), float(row["y"]), float(row["visibility"] or 0))
    full = [(ts, lm) for ts, lm in frames if all(i in lm for i in (11, 12, 23, 24, 25, 26, 27, 28))]
    return full, len(frames), w, h


results = []
for n in range(1, 21):
    for view in ("side", "front"):
        if not os.path.exists(f"{sys.argv[1]}/{view}_{n}.csv"):
            continue
        frames, total, w, h = load(f"{sys.argv[1]}/{view}_{n}.csv")
        reps, partials, ignored, detected = replay(frames, w, h)
        results.append(dict(person=n, filmed=view, frames=total, detected=detected, reps=reps, partials=partials, ignored=ignored))
        print(f"person {n:2d} {view:5s} frames {total:4d} detected {detected:4d} reps {len(reps)} partial {len(partials)} ignored {len(ignored)}",
              file=sys.stderr)
json.dump(results, sys.stdout, ensure_ascii=False)
