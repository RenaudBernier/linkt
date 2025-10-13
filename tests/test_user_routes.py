"""
Tests for User API routes
"""
import requests
import json

BASE_URL = "http://localhost:8080/api/users"
AUTH_URL = "http://localhost:8080/api/auth"

def get_auth_token():
    """Helper function to get authentication token"""
    # Register a test user
    payload = {
        "email": "user_test@example.com",
        "firstName": "User",
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
            "email": "user_test@example.com",
            "password": "password123"
        }
        response = requests.post(f"{AUTH_URL}/login", json=login_payload)
        return response.json()["token"]


def test_get_current_user():
    """Test GET /api/users/me"""
    print("\n=== Testing GET /api/users/me ===")

    token = get_auth_token()
    headers = {"Authorization": f"Bearer {token}"}

    response = requests.get(f"{BASE_URL}/me", headers=headers)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Validate response
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()

    # Validate user structure
    required_fields = ["userId", "email", "firstName", "lastName", "userType"]
    for field in required_fields:
        assert field in data, f"Missing required field: {field}"

    # Type validation
    assert isinstance(data["userId"], int), "userId should be integer"
    assert isinstance(data["email"], str), "email should be string"
    assert isinstance(data["firstName"], str), "firstName should be string"
    assert isinstance(data["lastName"], str), "lastName should be string"
    assert isinstance(data["userType"], str), "userType should be string"

    # Value validation
    assert data["email"] == "user_test@example.com", "email mismatch"
    assert data["firstName"] == "User", "firstName mismatch"
    assert data["lastName"] == "Test", "lastName mismatch"
    assert data["userType"] in ["student", "organizer", "administrator"], "Invalid userType"

    # Ensure password is NOT returned
    assert "password" not in data, "Password should not be returned in response"

    print("✅ Get current user test passed!")


def test_unauthorized_access():
    """Test accessing user info without authentication"""
    print("\n=== Testing unauthorized access ===")

    response = requests.get(f"{BASE_URL}/me")

    print(f"Status Code: {response.status_code}")

    # Should return 401 or 403
    assert response.status_code in [401, 403], f"Expected 401/403, got {response.status_code}"

    print("✅ Unauthorized access test passed!")


if __name__ == "__main__":
    try:
        test_get_current_user()
        test_unauthorized_access()
        print("\n✅ All user tests passed!")
    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
    except requests.exceptions.ConnectionError:
        print("\n❌ Connection error: Make sure the backend server is running on http://localhost:8080")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
