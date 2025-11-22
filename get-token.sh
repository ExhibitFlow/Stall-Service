#!/bin/bash
# Keycloak Authentication Helper Script
# This script helps you get access tokens for testing the Stall Service API

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
REALM="exhibitflow"
CLIENT_ID="stall-service"
CLIENT_SECRET="stall-service-secret-key-2024"

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     Keycloak Token Generator for Stall Service          ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# Function to get token
get_token() {
    local grant_type=$1
    local username=$2
    local password=$3
    local description=$4
    
    echo "Getting token for: $description"
    
    if [ "$grant_type" == "client_credentials" ]; then
        RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "grant_type=client_credentials" \
            -d "client_id=$CLIENT_ID" \
            -d "client_secret=$CLIENT_SECRET")
    else
        RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "grant_type=password" \
            -d "client_id=$CLIENT_ID" \
            -d "client_secret=$CLIENT_SECRET" \
            -d "username=$username" \
            -d "password=$password")
    fi
    
    TOKEN=$(echo $RESPONSE | jq -r '.access_token')
    
    if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
        echo "✓ Token obtained successfully"
        echo ""
        echo "Access Token:"
        echo "$TOKEN"
        echo ""
        echo "To use with curl:"
        echo "export TOKEN='$TOKEN'"
        echo 'curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/stalls'
        echo ""
        echo "════════════════════════════════════════════════════════════"
        echo ""
    else
        echo "✗ Failed to get token"
        echo "Response: $RESPONSE"
        echo ""
    fi
}

# Main menu
while true; do
    echo "Select user to authenticate:"
    echo "1) Service Account (client_credentials)"
    echo "2) Admin User (admin/admin123)"
    echo "3) Manager User (manager/manager123)"
    echo "4) Viewer User (viewer/viewer123)"
    echo "5) Test All Endpoints with Admin Token"
    echo "6) Exit"
    echo ""
    read -p "Enter choice [1-6]: " choice
    echo ""
    
    case $choice in
        1)
            get_token "client_credentials" "" "" "Service Account"
            ;;
        2)
            get_token "password" "admin" "admin123" "Admin User"
            ;;
        3)
            get_token "password" "manager" "manager123" "Manager User"
            ;;
        4)
            get_token "password" "viewer" "viewer123" "Viewer User"
            ;;
        5)
            echo "Getting admin token and testing endpoints..."
            RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
                -H "Content-Type: application/x-www-form-urlencoded" \
                -d "grant_type=password" \
                -d "client_id=$CLIENT_ID" \
                -d "client_secret=$CLIENT_SECRET" \
                -d "username=admin" \
                -d "password=admin123")
            
            TOKEN=$(echo $RESPONSE | jq -r '.access_token')
            
            if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
                echo "✓ Token obtained"
                echo ""
                echo "Testing GET /api/stalls..."
                curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/stalls | jq '{totalElements, numberOfElements, codes: [.content[0:3][].code]}'
                echo ""
                echo "Testing GET /api/stalls/code/A-001..."
                curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/stalls/code/A-001 | jq '{code, size, location, price, status}'
                echo ""
                echo "Testing Filter - Available Large Stalls..."
                curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8081/api/stalls?status=AVAILABLE&stallSize=LARGE" | jq '{totalElements, stalls: [.content[0:3][] | {code, location, price}]}'
                echo ""
            else
                echo "✗ Failed to get token"
            fi
            ;;
        6)
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid choice. Please try again."
            echo ""
            ;;
    esac
done
