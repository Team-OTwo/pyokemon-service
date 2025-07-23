#!/bin/bash

# JWT 테스트 스크립트
BASE_URL="http://localhost:8081"

echo "=== JWT 테스트 시작 ==="
echo ""

# 1. 로그인 테스트
echo "1. 로그인 테스트"
echo "POST $BASE_URL/api/admin/auth/login"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

echo "응답: $LOGIN_RESPONSE"
echo ""

# JWT 토큰 추출
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ 로그인 실패 - 액세스 토큰을 추출할 수 없습니다."
    exit 1
fi

echo "✅ 로그인 성공"
echo "액세스 토큰: ${ACCESS_TOKEN:0:50}..."
echo "리프레시 토큰: ${REFRESH_TOKEN:0:50}..."
echo ""

# 2. 토큰 검증 테스트
echo "2. 토큰 검증 테스트"
echo "GET $BASE_URL/api/admin/auth/verify"
VERIFY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/auth/verify" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "응답: $VERIFY_RESPONSE"
echo ""

# 3. 보호된 리소스 접근 테스트
echo "3. 보호된 리소스 접근 테스트"
echo "GET $BASE_URL/api/admin/auth/protected"
PROTECTED_RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/auth/protected" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "응답: $PROTECTED_RESPONSE"
echo ""

# 4. 토큰 갱신 테스트
echo "4. 토큰 갱신 테스트"
echo "POST $BASE_URL/api/admin/auth/refresh"
REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "\"$REFRESH_TOKEN\"")

echo "응답: $REFRESH_RESPONSE"
echo ""

# 5. 잘못된 토큰 테스트
echo "5. 잘못된 토큰 테스트"
echo "GET $BASE_URL/api/admin/auth/verify"
INVALID_RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/auth/verify" \
  -H "Authorization: Bearer invalid_token_here")

echo "응답: $INVALID_RESPONSE"
echo ""

echo "=== JWT 테스트 완료 ===" 