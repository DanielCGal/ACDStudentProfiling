CREATE TABLE IF NOT EXISTS public.accounts (
  account_id BIGINT PRIMARY KEY,
  account_password VARCHAR(255),
  student_email VARCHAR(255),
  user_role VARCHAR(255)
);

-- insert programheads/dean/secretary email and their chosen password in the parameters below

-- Example:
-- (1, 'programheadbsit password', 'programheadbsit gmail.com', 'program head/bsit "dont change anything in this parameter" ' )

INSERT INTO public.accounts (account_id, account_password, student_email, user_role) VALUES
  (1, 'password123', 'programheadbsit@gmail.com', 'program head/bsit'),
  (2, 'password123', 'programheadbsba@gmail.com', 'program head/bsba'),
  (3, 'password123', 'programheadbsed@gmail.com', 'program head/bsed'),
  (4, 'password123', 'programheadbeed@gmail.com', 'program head/beed'),
  (5, 'password123', 'programheadbshm@gmail.com', 'program head/bshm'),
  (6, 'password123', 'programheadbssw@gmail.com', 'program head/bssw'),
  (7, 'password123', 'dean@gmail.com', 'dean'),
  (8, 'password123', 'secretary@gmail.com', 'secretary')
ON CONFLICT (account_id) DO NOTHING;