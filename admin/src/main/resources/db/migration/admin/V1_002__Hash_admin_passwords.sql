-- 기존 관리자 계정의 비밀번호를 BCrypt 해싱된 값으로 업데이트
-- admin123 비밀번호의 BCrypt 해싱 값으로 업데이트
-- BCrypt 해싱된 값: $2a$10$6oWzULtSt38YKgEO8MZF7.9HEq7PGZ9.XC4FbDAVSz9DNPp9T1pAy

UPDATE tb_admin 
SET password = '$2a$10$6oWzULtSt38YKgEO8MZF7.9HEq7PGZ9.XC4FbDAVSz9DNPp9T1pAy'
WHERE username = 'admin' AND password = 'admin123';

-- 다른 관리자 계정이 있다면 여기에 추가 