"""
Tests for Event API routes
"""
import requests
import json

BASE_URL = "http://localhost:8080/api/events"

def test_get_all_events():
    """Test GET /api/events"""
    print("\n=== Testing GET /api/events ===")

    response = requests.get(BASE_URL)

    print(f"Status Code: {response.status_code}")

    # Validate response
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    assert isinstance(data, list), "Response should be a list"

    if len(data) > 0:
        event = data[0]
        print(f"Sample Event: {json.dumps(event, indent=2)}")

        # Validate event structure
        required_fields = ["eventId", "title", "description", "eventType",
                          "startDateTime", "endDateTime", "location", "capacity", "price"]

        for field in required_fields:
            assert field in event, f"Missing required field: {field}"

        # Type validation
        assert isinstance(event["eventId"], int), "eventId should be integer"
        assert isinstance(event["title"], str), "title should be string"
        assert isinstance(event["description"], str), "description should be string"
        assert isinstance(event["eventType"], str), "eventType should be string"
        assert isinstance(event["startDateTime"], str), "startDateTime should be string"
        assert isinstance(event["endDateTime"], str), "endDateTime should be string"
        assert isinstance(event["location"], str), "location should be string"
        assert isinstance(event["capacity"], int), "capacity should be integer"
        assert isinstance(event["price"], (int, float)), "price should be number"

        # Validate organizer object
        if "organizer" in event:
            organizer = event["organizer"]
            assert isinstance(organizer, dict), "organizer should be object"
            assert "userId" in organizer, "organizer missing userId"
            assert "email" in organizer, "organizer missing email"
            assert "firstName" in organizer, "organizer missing firstName"
            assert "lastName" in organizer, "organizer missing lastName"

        print(f"✅ Found {len(data)} events")

    print("✅ Get all events test passed!")


def test_get_event_by_id():
    """Test GET /api/events/{id}"""
    print("\n=== Testing GET /api/events/{id} ===")

    # Get all events first to get a valid ID
    all_events_response = requests.get(BASE_URL)
    events = all_events_response.json()

    if len(events) == 0:
        print("⚠️  No events available to test GET by ID")
        return

    event_id = events[0]["eventId"]
    response = requests.get(f"{BASE_URL}/{event_id}")

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Validate response
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    assert isinstance(data, dict), "Response should be an object"

    # Validate event structure
    required_fields = ["eventId", "title", "description", "eventType",
                      "startDateTime", "endDateTime", "location", "capacity", "price"]

    for field in required_fields:
        assert field in data, f"Missing required field: {field}"

    assert data["eventId"] == event_id, "eventId mismatch"

    print("✅ Get event by ID test passed!")


def test_get_nonexistent_event():
    """Test GET /api/events/{id} with non-existent ID"""
    print("\n=== Testing GET /api/events/99999 (non-existent) ===")

    response = requests.get(f"{BASE_URL}/99999")

    print(f"Status Code: {response.status_code}")

    # Should return 404
    assert response.status_code == 404, f"Expected 404, got {response.status_code}"

    print("✅ Non-existent event test passed!")


if __name__ == "__main__":
    try:
        test_get_all_events()
        test_get_event_by_id()
        test_get_nonexistent_event()
        print("\n✅ All event tests passed!")
    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
    except requests.exceptions.ConnectionError:
        print("\n❌ Connection error: Make sure the backend server is running on http://localhost:8080")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
