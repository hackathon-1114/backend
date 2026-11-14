#!/bin/sh
# 공개 스쿼트 데이터셋의 MediaPipe 관절 좌표를 내려받아, 규칙이 쓰는 관절 8개만 남긴다.
# 사용법: sh tools/verify/fetch.sh <저장할 폴더>
set -e
out="${1:?저장할 폴더를 적어 주세요}"
base=https://raw.githubusercontent.com/nihar787airobotics/squat_dataset_publication/main/data/processed/keypoints
mkdir -p "$out"
for n in $(seq 1 20); do
  for pair in "side/side_person$n.csv:side_$n.csv" "front/person_$n.csv:front_$n.csv"; do
    src="${pair%%:*}"; dst="$out/${pair##*:}"
    [ -s "$dst" ] && continue
    if curl -sf --compressed --retry 6 --retry-all-errors -m 180 -o "$dst.tmp" "$base/$src"; then
      awk -F, 'NR==1 || $8==11 || $8==12 || ($8>=23 && $8<=28)' "$dst.tmp" > "$dst"
    else
      echo "받지 못함: $src" >&2
    fi
    rm -f "$dst.tmp"
  done
done
ls "$out" | wc -l
