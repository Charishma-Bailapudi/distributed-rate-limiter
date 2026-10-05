import http from 'k6/http';
import { check } from 'k6';
export const options = { vus: 10, duration: '15s' };
export default function () {
  const res = http.get('http://localhost:8080/api/demo');
  check(res, { 'accepted or limited': r => r.status === 200 || r.status === 429 });
}