import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const successRate = new Counter('successful_requests');
const failRate    = new Counter('failed_requests');
const durationTrend = new Trend('request_duration', true);

export const options = {
  stages: [
    { duration: '30s', target: 30 },
    { duration: '1m30s', target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    failed_requests:   ['count<10'],
  },
};

export function setup() {
  const res = http.post('http://localhost:8080/api/apikeys/generate', null, {
    headers: { 'X-Client-Id': 'CLIENT-001' },
  });
  return { apiKey: JSON.parse(res.body).apiKey };
}

export default function (data) {
  group('API Key Validation', function () {
    const res = http.post('http://localhost:8080/api/apikeys/validate', null, {
      headers: {
        'X-Client-Id': 'CLIENT-001',
        'X-Api-Key':   data.apiKey,
      },
      timeout: '30s',
    });

    const ok = check(res, {
      'status 200': (r) => r.status === 200,
      'apiKey is true': (r) => {
        try { return JSON.parse(r.body).apiKey === 'true'; } catch { return false; }
      },
    });

    durationTrend.add(res.timings.duration);
    if (ok) successRate.add(1);
    else     failRate.add(1);
  });

  sleep(0.5 + Math.random() * 1.5);
}
