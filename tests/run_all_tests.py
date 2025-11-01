"""
Run all API tests
"""
import subprocess
import sys

TEST_FILES = [
    "test_auth_routes.py",
    "test_event_routes.py",
    "test_user_routes.py",
    "test_ticket_routes.py",
    "test_saved_events_routes.py",
    "test_registered_students.py"
]

def run_test(test_file):
    """Run a single test file"""
    print(f"\n{'='*60}")
    print(f"Running: {test_file}")
    print(f"{'='*60}")

    try:
        result = subprocess.run(
            [sys.executable, test_file],
            capture_output=False,
            text=True
        )
        return result.returncode == 0
    except Exception as e:
        print(f"Error running {test_file}: {e}")
        return False


def main():
    """Run all tests"""
    print("\n" + "="*60)
    print("LINKT API TEST SUITE")
    print("="*60)
    print("\nMake sure the backend server is running on http://localhost:8080")
    print("Press Enter to continue or Ctrl+C to cancel...")
    input()

    results = {}

    for test_file in TEST_FILES:
        success = run_test(test_file)
        results[test_file] = success

    # Print summary
    print("\n" + "="*60)
    print("TEST SUMMARY")
    print("="*60)

    passed = sum(1 for v in results.values() if v)
    total = len(results)

    for test_file, success in results.items():
        status = "✅ PASSED" if success else "❌ FAILED"
        print(f"{status}: {test_file}")

    print(f"\nTotal: {passed}/{total} test files passed")

    if passed == total:
        print("\n🎉 All tests passed!")
        return 0
    else:
        print(f"\n⚠️  {total - passed} test file(s) failed")
        return 1


if __name__ == "__main__":
    sys.exit(main())
