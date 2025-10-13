"""
Tests for Authentication API routes
"""
import requests
import json

BASE_URL = "http://localhost:8080/api/auth"

def test_register():
    """Test POST /api/auth/register"""
    print("\n=== Testing POST /api/auth/register ===")

    # Test data
    payload = {
        "email": "test_user@example.com",
        "firstName": "Test",
        "lastName": "User",
        "password": "password123",
        "userType": "student"
    }

    response = requests.post(f"{BASE_URL}/register", json=payload)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Validate response structure
    if response.status_code == 200:
        data = response.json()
        assert "token" in data, "Missing 'token' field"
        assert "userId" in data, "Missing 'userId' field"
        assert "email" in data, "Missing 'email' field"
        assert "firstName" in data, "Missing 'firstName' field"
        assert "lastName" in data, "Missing 'lastName' field"
        assert "userType" in data, "Missing 'userType' field"

        assert isinstance(data["token"], str), "token should be string"
        assert isinstance(data["userId"], int), "userId should be integer"
        assert isinstance(data["email"], str), "email should be string"
        assert data["email"] == payload["email"], "email mismatch"
        assert data["userType"] == payload["userType"], "userType mismatch"

        print("✅ Register test passed!")
        return data["token"]
    elif response.status_code == 400:
        print("⚠️  User already exists (expected if run multiple times)")
        return None
    else:
        raise AssertionError(f"Unexpected status code: {response.status_code}")


def test_login():
    """Test POST /api/auth/login"""
    print("\n=== Testing POST /api/auth/login ===")

    # First register a user
    register_payload = {
        "email": "login_test@example.com",
        "firstName": "Login",
        "lastName": "Test",
        "password": "password123",
        "userType": "student"
    }
    requests.post(f"{BASE_URL}/register", json=register_payload)

    # Test login
    login_payload = {
        "email": "login_test@example.com",
        "password": "password123"
    }

    response = requests.post(f"{BASE_URL}/login", json=login_payload)

    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")

    # Validate response structure
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"

    data = response.json()
    assert "token" in data, "Missing 'token' field"
    assert "userId" in data, "Missing 'userId' field"
    assert "email" in data, "Missing 'email' field"
    assert "firstName" in data, "Missing 'firstName' field"
    assert "lastName" in data, "Missing 'lastName' field"
    assert "userType" in data, "Missing 'userType' field"

    assert isinstance(data["token"], str), "token should be string"
    assert isinstance(data["userId"], int), "userId should be integer"
    assert data["email"] == login_payload["email"], "email mismatch"

    print("✅ Login test passed!")
    return data["token"]


if __name__ == "__main__":
    try:
        test_register()
        test_login()
        print("\n✅ All authentication tests passed!")
    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
    except requests.exceptions.ConnectionError:
        print("\n❌ Connection error: Make sure the backend server is running on http://localhost:8080")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
