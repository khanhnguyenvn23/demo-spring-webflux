import argparse
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from urllib.error import HTTPError, URLError
from urllib.request import urlopen


ENDPOINTS = (
    ("Spring MVC", "http://localhost:8081/api/demo/blocking"),
    ("Spring WebFlux", "http://localhost:8080/api/demo/non-blocking"),
)


def bounded_int(value, maximum):
    number = int(value)
    if number < 1 or number > maximum:
        raise argparse.ArgumentTypeError(f"value must be between 1 and {maximum}")
    return number


def request_once(url):
    start = time.perf_counter()
    try:
        with urlopen(url, timeout=30) as response:
            response.read()
            elapsed = time.perf_counter() - start
            ok = 200 <= response.status < 300
            return ok, elapsed, "" if ok else f"HTTP {response.status}"
    except (HTTPError, URLError, TimeoutError, OSError) as error:
        return False, time.perf_counter() - start, str(error)


def run_test(name, url, requests, concurrency):
    start = time.perf_counter()
    results = []

    with ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(request_once, url) for _ in range(requests)]
        for future in as_completed(futures):
            results.append(future.result())

    elapsed = time.perf_counter() - start
    succeeded = sum(1 for ok, _, _ in results if ok)
    latencies = [duration for _, duration, _ in results]
    print(
        f"{name}: {succeeded}/{requests} passed | "
        f"total {elapsed:.2f}s | "
        f"average {sum(latencies) / len(latencies):.2f}s | "
        f"max {max(latencies):.2f}s"
    )

    failures = [error for ok, _, error in results if not ok]
    for error in failures[:3]:
        print(f"  Request error: {error}")
    return succeeded == requests


def main():
    parser = argparse.ArgumentParser(
        description="Run a bounded load comparison against the two localhost apps."
    )
    parser.add_argument(
        "--requests",
        type=lambda value: bounded_int(value, 300),
        default=40,
        help="Requests per app (1-300, default: 40)",
    )
    parser.add_argument(
        "--concurrency",
        type=lambda value: bounded_int(value, 250),
        default=20,
        help="Concurrent requests (1-250, default: 20)",
    )
    args = parser.parse_args()

    if args.concurrency > args.requests:
        parser.error("--concurrency cannot be greater than --requests")

    print(
        f"Running {args.requests} requests/app, up to {args.concurrency} concurrently.\n"
        "Requests are restricted to localhost and run sequentially per app."
    )
    results = [
        run_test(name, url, args.requests, args.concurrency)
        for name, url in ENDPOINTS
    ]
    return 0 if all(results) else 1


if __name__ == "__main__":
    raise SystemExit(main())
