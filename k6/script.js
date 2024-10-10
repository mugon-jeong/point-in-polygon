import http from "k6/http";
import { check, sleep } from "k6";

// 단계적 부하 증가를 위한 옵션 설정
// export let options = {
//   stages: [
//     { duration: "1m", target: 100 }, // 1분 동안 50명의 사용자
//     { duration: "1m", target: 200 }, // 1분 동안 100명의 사용자
//     { duration: "1m", target: 300 }, // 1분 동안 200명의 사용자
//     { duration: "1m", target: 400 }, // 1분 동안 200명의 사용자
//     { duration: "1m", target: 500 }, // 1분 동안 200명의 사용자
//     { duration: "1m", target: 0 }, // 1분 동안 부하 감소
//   ],
// };

export const options = {
  vus: 1000,
  duration: "300s",
};

// 좌표 리스트 (폴리곤)
const polygonCoords = [
  [127.75410902052126, 34.86230891834978],
  [127.7621127322217, 34.864765005500765],
  [127.76505243330202, 34.86435126118972],
  [127.76498806028566, 34.864060758621136],
  [127.76237022428712, 34.86441288281477],
  [127.76225220709047, 34.86431604881183],
  [127.762348766615, 34.86351496314145],
  [127.75495659856996, 34.86185994825318],
  [127.75488149671754, 34.86225609846563],
  [127.75472056417665, 34.862317721660524],
  [127.75418412237367, 34.862132851937325],
  [127.75410902052126, 34.86230891834978],
];

// 폴리곤 내부에 있는지 확인하는 함수 (Ray-casting algorithm)
function isPointInPolygon(point, polygon) {
  const [x, y] = point;
  let isInside = false;
  for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
    const [xi, yi] = polygon[i];
    const [xj, yj] = polygon[j];
    const intersect =
        yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi;
    if (intersect) isInside = !isInside;
  }
  return isInside;
}

// 폴리곤 안에서 랜덤한 좌표 생성
// 폴리곤 안에서 랜덤한 좌표 생성
function generateRandomCoords(polygon, probability) {
  let minX = Math.min(...polygon.map((coord) => coord[0]));
  let maxX = Math.max(...polygon.map((coord) => coord[0]));
  let minY = Math.min(...polygon.map((coord) => coord[1]));
  let maxY = Math.max(...polygon.map((coord) => coord[1]));

  let randomPoint;
  const randomChance = Math.random(); // 0과 1 사이의 랜덤 값 생성

  if (randomChance <= probability) {
    // 지정된 확률 내에서 폴리곤 안의 좌표를 생성
    do {
      const randomX = Math.random() * (maxX - minX) + minX;
      const randomY = Math.random() * (maxY - minY) + minY;
      randomPoint = [randomX, randomY];
    } while (!isPointInPolygon(randomPoint, polygon)); // 폴리곤 안에 있는지 확인
  } else {
    // 확률 외의 경우, 폴리곤 밖의 좌표를 생성
    do {
      const randomX = Math.random() * (maxX - minX) + minX;
      const randomY = Math.random() * (maxY - minY) + minY;
      randomPoint = [randomX, randomY];
    } while (isPointInPolygon(randomPoint, polygon)); // 폴리곤 밖에 있는지 확인
  }

  return randomPoint;
}

export default function () {
  const randomCoords = generateRandomCoords(polygonCoords, 0.5);
  const byDb = "st-within";
  const byServer = "within";
  const url = `http://172.30.1.114:8780/territories/1/${byDb}?long=${randomCoords[0]}&lat=${randomCoords[1]}`;

  // GET 요청 실행
  let res = http.get(url);

  // 응답 검사
  check(res, {
    "status is 200": (r) => r.status === 200,
  });

  // 2초 대기
  sleep(1);
}
