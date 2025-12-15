import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    stages: [
        { duration: '5s', target: 100 },  // Warm up
        { duration: '10s', target: 1000 }, // High Load
        { duration: '5s', target: 0 },    // Cool down
    ],
    thresholds: {
        http_req_duration: ['p(95)<100'], // Expect fast responses!
    },
};

export default function () {
    // FIX: Using Query Parameters instead of JSON Body
    const productId = 1;
    const userId = Math.floor(Math.random() * 10000) + 1;

    // URL with params
    const url = `http://localhost:8080/api/engine/v1/orders?productId=${productId}&userId=${userId}`;

    const params = {
        headers: {
            'Idempotency-Key': uuidv4(), // Unique key

        },
    };

    // Using POST with empty body (since params are in URL)
    // If your controller uses @RequestBody, keep the JSON body!
    // But based on previous chat, you used @RequestParam.
    const res = http.post(url, null, params);

    // Debugging: If it fails, print the error code once to see what's wrong
    if (res.status !== 200) {
        console.log(`Error: ${res.status}`);
    }

    check(res, {
        'is status 200': (r) => r.status === 200,
    });
}