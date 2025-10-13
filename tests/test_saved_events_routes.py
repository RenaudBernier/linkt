"""
Tests for Saved Events API routes
"""
import requests
import json

BASE_URL = "http://localhost:8080/api/saved-events"
AUTH_URL = "http://localhost:8080/api/auth"

def get_auth_token():
    """Helper function to get authentication token"""
    # Register a test user
    payload = {
        "email": "saved_events_test@example.com",
        "firstName": "SavedEvents",
        "lastName": "Test",
        "password": "password123",
        "userType": "student"
    }

    response = requests.post(f"{AUTH_URL}/register", json=payload)
    if response.status_code == 200:
        return response.json()["token"]
    else:
        # User already exists, login instead
        login_payload = {
            "email": "saved_events_test@example.com",
            "password": "password123"
        }
        response = requests.post(f"{AUTH_URL}/login", json=login_payload)
        return response.json()["token"]


def test_save_event():
    """Test POST /api/saved-events"""
    print("\n=== Testing POST /api/saved-events ===")

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    # Save event 2
    payload = {"eventId": 2}

    response = requests.post(BASE_URL, json=payload, headers=headers)

    print(f"Status Code: {response.status_code}")

    # Should return 201 or 409 (if already saved)
    assert response.status_code in [201, 409], f"Expected 201/409, got {response.status_code}"

    if response.status_code == 201:
        data = response.json()
        print(f"Response: {json.dumps(data, indent=2)}")

        # Validate saved event structure
        required_fields = ["savedEventId", "student", "event"]
        for field in required_fields:
            assert field in data, f"Missing required field: {field}"

        # Validate student object
        student = data["student"]
        assert isinstance(student, dict), "student should be object"
        assert "userId" in student, "student missing userId"

        # Validate event object
        event = data["event"]
        assert isinstance(event, dict), "event should be object"
        assert "eventId" in event, "event missing eventId"
        assert event["eventId"] == 2, "eventId mismatch"

        print("✅ Save event test passed!")
    else:
        print("⚠️  Event already saved (expected if run multiple times)")


def test_get_my_saved_events():
    """Test GET /api/saved-events/me"""
    print("\n=== Testing GET /api/saved-events/me ===")

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    response = requests.get(f"{BASE_URL}/me", headers=headers)

    print(f"Status Code: {response.status_code}")

    # Validate response
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    assert isinstance(data, list), "Response should be a list"

    print(f"Found {len(data)} saved events")

    if len(data) > 0:
        event = data[0]
        print(f"Sample Saved Event: {json.dumps(event, indent=2)}")

        # IMPORTANT: Response should be Event objects, NOT SavedEvent objects
        required_fields = ["eventId", "title", "description", "eventType",
                          "startDateTime", "endDateTime", "location", "capacity", "price"]

        for field in required_fields:
            assert field in event, f"Missing required field: {field}"

        # Validate types
        assert isinstance(event["eventId"], int), "eventId should be integer"
        assert isinstance(event["title"], str), "title should be string"
        assert isinstance(event["price"], (int, float)), "price should be number"

    print("✅ Get my saved events test passed!")


def test_check_if_saved():
    """Test GET /api/saved-events/check/{eventId}"""
    print("\n=== Testing GET /api/saved-events/check/{eventId} ===")

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    # First save an event
    save_payload = {"eventId": 3}
    requests.post(BASE_URL, json=save_payload, headers=headers)

    # Check if saved
    response = requests.get(f"{BASE_URL}/check/3", headers=headers)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Validate response
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    assert "isSaved" in data, "Missing 'isSaved' field"
    assert isinstance(data["isSaved"], bool), "isSaved should be boolean"
    assert data["isSaved"] == True, "Event should be saved"

    print("✅ Check if saved test passed!")


def test_unsave_event():
    """Test DELETE /api/saved-events/event/{eventId}"""
    print("\n=== Testing DELETE /api/saved-events/event/{eventId} ===")

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    # First save an event
    save_payload = {"eventId": 2}
    requests.post(BASE_URL, json=save_payload, headers=headers)

    # Then unsave it
    response = requests.delete(f"{BASE_URL}/event/2", headers=headers)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Validate response
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    assert "message" in data, "Missing 'message' field"

    # Verify it's no longer saved
    check_response = requests.get(f"{BASE_URL}/check/2", headers=headers)
    check_data = check_response.json()
    assert check_data["isSaved"] == False, "Event should no longer be saved"

    print("✅ Unsave event test passed!")


def test_unauthorized_access():
    """Test accessing saved events without authentication"""
    print("\n=== Testing unauthorized access ===")

    response = requests.get(f"{BASE_URL}/me")

    print(f"Status Code: {response.status_code}")

    # Should return 401 or 403
    assert response.status_code in [401, 403], f"Expected 401/403, got {response.status_code}"

    print("✅ Unauthorized access test passed!")


if __name__ == "__main__":
    try:
        test_save_event()
        test_get_my_saved_events()
        test_check_if_saved()
        test_unsave_event()
        test_unauthorized_access()
        print("\n✅ All saved events tests passed!")
    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
    except requests.exceptions.ConnectionError:
        print("\n❌ Connection error: Make sure the backend server is running on http://localhost:8080")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
