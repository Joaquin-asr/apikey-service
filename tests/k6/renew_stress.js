import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const successRate  = new Counter('successful_requests');
const cooldownRate = new Counter('cooldown_responses');
const failRate     = new Counter('failed_requests');
const durationTrend = new Trend('request_duration', true);

const COOLDOWN_CODE = 'RENEW_COOLDOWN_ACTIVE';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 20 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    failed_requests:   ['count<5'],
  },
};

export default function () {
  group('API Key Renew', function () {
    const res = http.post('http://localhost:8080/api/apikeys/renew', null, {
      headers: {
        'X-Client-Id':        'CLIENT-001',
        'X-Renewed-ApiKey-Id': 'RENEW-SECRET-KEY-001',
      },
      timeout: '30s',
    });

    durationTrend.add(res.timings.duration);

    let body;
    try { body = JSON.parse(res.body); } catch { body = {}; }

    const renewed  = res.status === 200 && body.success === true;
    const cooldown = res.status === 200 && body.code === COOLDOWN_CODE;
    const error    = !renewed && !cooldown;

    check(res, {
      'status 200':       (r) => r.status === 200,
      'no server error':  (r) => r.status !== 500,
    });

    if (renewed)  successRate.add(1);
    else if (cooldown) cooldownRate.add(1);
    else          failRate.add(1);
  });

  sleep(0.5 + Math.random() * 1.5);
}
