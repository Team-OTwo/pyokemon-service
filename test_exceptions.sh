#!/bin/bash

BASE_URL="http://localhost:8080/account"

echo "=== AccountErrorCodes 예외 처리 테스트 ==="
echo

# 1. 인증 정보 없이 접근 (ACCESS_DENIED 예상)
echo "1. 인증 정보 없이 접근 테스트 (ACCESS_DENIED 예상)"
curl -X GET "$BASE_URL/api/tenants/profile" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "2. 잘못된 인증 헤더로 접근 테스트 (ACCESS_DENIED 예상)"
curl -X GET "$BASE_URL/api/tenants/profile" \
  -H "Content-Type: application/json" \
  -H "X-Auth-AccountId: invalid" \
  -H "X-Auth-Role: INVALID" \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "3. 권한이 없는 API 접근 테스트 (PERMISSION_DENIED 예상)"
curl -X GET "$BASE_URL/api/admin/users" \
  -H "Content-Type: application/json" \
  -H "X-Auth-AccountId: 1" \
  -H "X-Auth-Role: TENANT" \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "4. 잘못된 로그인 정보로 로그인 테스트 (INVALID_LOGIN 예상)"
curl -X POST "$BASE_URL/api/login" \
  -H "Content-Type: application/json" \
  -d '{"loginId":"nonexistent","password":"wrong"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "5. 중복된 로그인 ID로 테넌트 등록 테스트 (DUPLICATE_LOGIN_ID 예상)"
curl -X POST "$BASE_URL/api/tenants" \
  -H "Content-Type: application/json" \
  -d '{"loginId":"test_tenant_001","password":"password123","name":"중복 테스트","corpId":"9999999999","city":"서울시","street":"테스트로 123","zipcode":"12345","ceo":"테스트"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "6. 중복된 사업자번호로 테넌트 등록 테스트 (DUPLICATE_CORP_ID 예상)"
curl -X POST "$BASE_URL/api/tenants" \
  -H "Content-Type: application/json" \
  -d '{"loginId":"new_tenant_001","password":"password123","name":"중복 사업자번호 테스트","corpId":"1234567890","city":"서울시","street":"테스트로 456","zipcode":"12345","ceo":"테스트"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "7. 존재하지 않는 테넌트 조회 테스트 (TENANT_NOT_FOUND 예상)"
curl -X GET "$BASE_URL/api/tenants/99999" \
  -H "Content-Type: application/json" \
  -H "X-Auth-AccountId: 1" \
  -H "X-Auth-Role: ADMIN" \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "8. 다른 사용자의 정보 조회 테스트 (ACCESS_DENIED 예상)"
curl -X GET "$BASE_URL/api/tenants/profile" \
  -H "Content-Type: application/json" \
  -H "X-Auth-AccountId: 999" \
  -H "X-Auth-Role: TENANT" \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "=== 테스트 완료 ===" 