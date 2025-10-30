"""
Tests for Ticket API routes
"""
import requests
import json

BASE_URL = "http://localhost:8080/api/tickets"
AUTH_URL = "http://localhost:8080/api/auth"

def get_auth_token():
    """Helper function to get authentication token"""
    # Register a test user
    payload = {
        "email": "ticket_test@example.com",
        "firstName": "Ticket",
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
            "email": "ticket_test@example.com",
            "password": "password123"
        }
        response = requests.post(f"{AUTH_URL}/login", json=login_payload)
        return response.json()["token"]


def test_buy_ticket():
    """Test POST /api/tickets"""
    print("\n=== Testing POST /api/tickets ===")

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    # Buy ticket for event 1
    payload = {"eventId": 1}

    response = requests.post(BASE_URL, json=payload, headers=headers)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Validate response
    assert response.status_code == 201, f"Expected 201, got {response.status_code}"

    data = response.json()

    # Validate ticket structure
    required_fields = ["ticketId", "qrCode", "student", "event"]
    for field in required_fields:
        assert field in data, f"Missing required field: {field}"

    # Type validation
    assert isinstance(data["ticketId"], int), "ticketId should be integer"
    assert isinstance(data["qrCode"], str), "qrCode should be string"
    assert len(data["qrCode"]) > 0, "qrCode should not be empty"

    # Validate student object
    student = data["student"]
    assert isinstance(student, dict), "student should be object"
    assert "userId" in student, "student missing userId"
    assert "email" in student, "student missing email"

    # Validate event object
    event = data["event"]
    assert isinstance(event, dict), "event should be object"
    assert "eventId" in event, "event missing eventId"
    assert "title" in event, "event missing title"
    assert event["eventId"] == 1, "eventId mismatch"

    print("✅ Buy ticket test passed!")
    return data["ticketId"]


def test_get_my_tickets():
    """Test GET /api/tickets/me"""
    print("\n=== Testing GET /api/tickets/me ===")

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    response = requests.get(f"{BASE_URL}/me", headers=headers)

    print(f"Status Code: {response.status_code}")

    # Validate response
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    assert isinstance(data, list), "Response should be a list"

    if len(data) > 0:
        ticket = data[0]
        print(f"Sample Ticket: {json.dumps(ticket, indent=2)}")

        # Validate ticket structure
        required_fields = ["ticketId", "qrCode", "student", "event"]
        for field in required_fields:
            assert field in ticket, f"Missing required field: {field}"

        # Validate QR code format: LINKT-{eventId}-{ticketId}
        qr_code = ticket["qrCode"]
        assert isinstance(qr_code, str), "qrCode should be string"
        assert qr_code.startswith("LINKT-"), f"qrCode should start with 'LINKT-', got: {qr_code}"
        qr_parts = qr_code.split("-")
        assert len(qr_parts) == 3, f"qrCode should have format LINKT-eventId-ticketId, got: {qr_code}"
        assert qr_parts[1].isdigit(), f"Event ID should be numeric, got: {qr_parts[1]}"
        assert qr_parts[2].isdigit(), f"Ticket ID should be numeric, got: {qr_parts[2]}"

        print(f"✅ Found {len(data)} tickets")

    print("✅ Get my tickets test passed!")


def test_get_ticket_by_id():
    """Test GET /api/tickets/{id}"""
    print("\n=== Testing GET /api/tickets/{id} ===")

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    # First get tickets to find a valid ID
    my_tickets_response = requests.get(f"{BASE_URL}/me", headers=headers)
    tickets = my_tickets_response.json()

    if len(tickets) == 0:
        print("⚠️  No tickets available to test GET by ID")
        return

    ticket_id = tickets[0]["ticketId"]
    response = requests.get(f"{BASE_URL}/{ticket_id}", headers=headers)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Validate response
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    assert data["ticketId"] == ticket_id, "ticketId mismatch"
    assert "qrCode" in data, "Missing qrCode"

    print("✅ Get ticket by ID test passed!")


def test_unauthorized_access():
    """Test accessing tickets without authentication"""
    print("\n=== Testing unauthorized access ===")

    response = requests.get(f"{BASE_URL}/me")

    print(f"Status Code: {response.status_code}")

    # Should return 401 or 403
    assert response.status_code in [401, 403], f"Expected 401/403, got {response.status_code}"

    print("✅ Unauthorized access test passed!")


if __name__ == "__main__":
    try:
        test_buy_ticket()
        test_get_my_tickets()
        test_get_ticket_by_id()
        test_unauthorized_access()
        print("\n✅ All ticket tests passed!")
    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
    except requests.exceptions.ConnectionError:
        print("\n❌ Connection error: Make sure the backend server is running on http://localhost:8080")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
