"""
Tests for Registered Students API routes
Tests the organizer view of registered students for events
"""
import requests
import json

BASE_URL = "http://localhost:8080/api/events"
AUTH_URL = "http://localhost:8080/api/auth"

def create_organizer_and_get_token():
    """Helper function to create an organizer account and return token"""
    print("\n=== Setting up organizer account ===")

    payload = {
        "email": f"organizer_test_{hash('registered_students_test')%10000}@example.com",
        "firstName": "Organizer",
        "lastName": "Test",
        "password": "password123",
        "userType": "organizer"
    }

    response = requests.post(f"{AUTH_URL}/register", json=payload)

    if response.status_code == 200:
        data = response.json()
        print(f"Organizer created with ID: {data.get('userId')}")
        return data.get("token"), data.get("userId")
    elif response.status_code == 400:
        # User already exists, try to login
        print("Organizer account already exists, attempting login...")
        login_payload = {
            "email": payload["email"],
            "password": payload["password"]
        }
        login_response = requests.post(f"{AUTH_URL}/login", json=login_payload)
        if login_response.status_code == 200:
            login_data = login_response.json()
            return login_data.get("token"), login_data.get("userId")

    return None, None


def get_organizer_events(token):
    """Helper function to get events created by the organizer"""
    print("\n=== Getting all events ===")

    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(BASE_URL, headers=headers)

    print(f"Status Code: {response.status_code}")

    if response.status_code == 200:
        events = response.json()
        print(f"Found {len(events)} events")
        return events
    else:
        print(f"Failed to get events: {response.text}")
        return []


def test_get_registered_students_unauthorized():
    """Test GET /api/events/{id}/registered-students without authentication"""
    print("\n=== Testing GET /api/events/1/registered-students (UNAUTHORIZED) ===")

    response = requests.get(f"{BASE_URL}/1/registered-students")

    print(f"Status Code: {response.status_code}")

    # Should return 401 or 403 for unauthorized access
    assert response.status_code in [401, 403], f"Expected 401 or 403, got {response.status_code}"

    print("✅ Unauthorized access test passed!")


def test_get_registered_students_forbidden():
    """Test GET /api/events/{id}/registered-students as non-organizer"""
    print("\n=== Testing GET /api/events/1/registered-students (FORBIDDEN - Student user) ===")

    # Create student account
    student_payload = {
        "email": f"student_test_{hash('forbidden_test')%10000}@example.com",
        "firstName": "Student",
        "lastName": "Test",
        "password": "password123",
        "userType": "student"
    }

    student_response = requests.post(f"{AUTH_URL}/register", json=student_payload)

    if student_response.status_code == 200:
        student_token = student_response.json().get("token")
    else:
        # Try login if already exists
        login_payload = {
            "email": student_payload["email"],
            "password": student_payload["password"]
        }
        login_response = requests.post(f"{AUTH_URL}/login", json=login_payload)
        student_token = login_response.json().get("token")

    headers = {"Authorization": f"Bearer {student_token}"}
    response = requests.get(f"{BASE_URL}/1/registered-students", headers=headers)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Should return 403 Forbidden for non-organizer
    assert response.status_code == 403, f"Expected 403, got {response.status_code}"
    assert "organizer" in response.json().get("error", "").lower() or "organizer" in str(response.json()).lower(), \
        "Response should indicate that only organizers can access this"

    print("✅ Forbidden access test passed!")


def test_get_registered_students_not_found():
    """Test GET /api/events/{id}/registered-students with non-existent event"""
    print("\n=== Testing GET /api/events/99999/registered-students (NOT FOUND) ===")

    token, _ = create_organizer_and_get_token()

    if not token:
        print("⚠️  Could not create/login organizer account, skipping test")
        return

    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{BASE_URL}/99999/registered-students", headers=headers)

    print(f"Status Code: {response.status_code}")

    # Should return 404 for non-existent event
    assert response.status_code == 404, f"Expected 404, got {response.status_code}"

    print("✅ Not found test passed!")


def test_get_registered_students_success():
    """Test GET /api/events/{id}/registered-students with valid organizer"""
    print("\n=== Testing GET /api/events/{id}/registered-students (SUCCESS) ===")

    token, organizer_id = create_organizer_and_get_token()

    if not token:
        print("⚠️  Could not create/login organizer account, skipping test")
        return

    # Get organizer's events
    headers = {"Authorization": f"Bearer {token}"}
    events = get_organizer_events(token)

    if not events:
        print("⚠️  No events available to test with")
        return

    # Test with first event
    event_id = events[0].get("eventId")
    print(f"\nTesting registered students for event ID: {event_id}")

    response = requests.get(f"{BASE_URL}/{event_id}/registered-students", headers=headers)

    print(f"Status Code: {response.status_code}")

    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    print(f"Response: {json.dumps(data, indent=2)}")

    # Response should be a list
    assert isinstance(data, list), "Response should be a list of registered students"

    # If there are registered students, validate structure
    if len(data) > 0:
        student = data[0]
        print(f"Sample registered student: {json.dumps(student, indent=2)}")

        # Validate required fields
        required_fields = ["userId", "firstName", "lastName", "email", "ticketId"]
        for field in required_fields:
            assert field in student, f"Missing required field: {field}"

        # Validate field types
        assert isinstance(student["userId"], int), "userId should be integer"
        assert isinstance(student["firstName"], str), "firstName should be string"
        assert isinstance(student["lastName"], str), "lastName should be string"
        assert isinstance(student["email"], str), "email should be string"
        assert isinstance(student["ticketId"], int), "ticketId should be integer"

        # Validate optional fields
        if "qrCode" in student:
            assert isinstance(student["qrCode"], str) or student["qrCode"] is None, "qrCode should be string or null"
        if "isScanned" in student:
            assert isinstance(student["isScanned"], bool), "isScanned should be boolean"
        if "scannedAt" in student:
            assert isinstance(student["scannedAt"], str) or student["scannedAt"] is None, "scannedAt should be string or null"

    print(f"✅ Found {len(data)} registered students")
    print("✅ Get registered students test passed!")


def test_permission_denied_for_other_organizer():
    """Test that organizer cannot view students for events they don't own"""
    print("\n=== Testing permission denial (other organizer) ===")

    # Create first organizer
    organizer1_payload = {
        "email": f"organizer1_{hash('perm_test1')%10000}@example.com",
        "firstName": "Organizer",
        "lastName": "One",
        "password": "password123",
        "userType": "organizer"
    }
    response1 = requests.post(f"{AUTH_URL}/register", json=organizer1_payload)

    if response1.status_code != 200:
        login_payload = {
            "email": organizer1_payload["email"],
            "password": organizer1_payload["password"]
        }
        response1 = requests.post(f"{AUTH_URL}/login", json=login_payload)

    organizer1_token = response1.json().get("token")

    # Create second organizer
    organizer2_payload = {
        "email": f"organizer2_{hash('perm_test2')%10000}@example.com",
        "firstName": "Organizer",
        "lastName": "Two",
        "password": "password123",
        "userType": "organizer"
    }
    response2 = requests.post(f"{AUTH_URL}/register", json=organizer2_payload)

    if response2.status_code != 200:
        login_payload = {
            "email": organizer2_payload["email"],
            "password": organizer2_payload["password"]
        }
        response2 = requests.post(f"{AUTH_URL}/login", json=login_payload)

    organizer2_token = response2.json().get("token")

    # Get organizer 1's events
    headers1 = {"Authorization": f"Bearer {organizer1_token}"}
    events = get_organizer_events(organizer1_token)

    if not events:
        print("⚠️  No events available to test permission denial")
        return

    event_id = events[0].get("eventId")

    # Try to access with organizer 2's token
    headers2 = {"Authorization": f"Bearer {organizer2_token}"}
    response = requests.get(f"{BASE_URL}/{event_id}/registered-students", headers=headers2)

    print(f"Status Code: {response.status_code}")

    # Should return 403 Forbidden
    assert response.status_code == 403, f"Expected 403, got {response.status_code}"
    assert "permission" in response.json().get("error", "").lower() or "permission" in str(response.json()).lower(), \
        "Response should indicate permission denied"

    print("✅ Permission denial test passed!")


if __name__ == "__main__":
    try:
        test_get_registered_students_unauthorized()
        test_get_registered_students_forbidden()
        test_get_registered_students_not_found()
        test_get_registered_students_success()
        test_permission_denied_for_other_organizer()
        print("\n✅ All registered students tests passed!")
    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
    except requests.exceptions.ConnectionError:
        print("\n❌ Connection error: Make sure the backend server is running on http://localhost:8080")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
