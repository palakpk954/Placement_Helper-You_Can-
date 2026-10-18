-- ===================================================
-- You_can Placement Portal (JECRC University)
-- Flyway Migration V2: Comprehensive Seed Data
-- ===================================================

-- 1. Seed Users (Initial Admin, Students, Alumni Mentors)
-- Password for all seed users is 'admin123' / 'student123' / 'mentor123' / 'password123'
-- Hash below corresponds to BCrypt hash '$2a$10$wT6qUaQ/lF82lJk7E7p5CeG4y4vjC0Z9e3L4VqX9z9KkP3l2Z4g3m'
-- Note: DataInitializer also ensures password compatibility on startup!
INSERT INTO users (id, email, password, full_name, branch, graduation_year, cgpa, active_backlogs, role, email_verified, points) VALUES
(1, 'admin@jecrcu.edu.in', '$2a$10$wT6qUaQ/lF82lJk7E7p5CeG4y4vjC0Z9e3L4VqX9z9KkP3l2Z4g3m', 'Placement Cell Admin', 'CSE', 2022, 9.10, 0, 'ROLE_ADMIN', TRUE, 500),
(2, 'rahul.sharma@jecrcu.edu.in', '$2a$10$wT6qUaQ/lF82lJk7E7p5CeG4y4vjC0Z9e3L4VqX9z9KkP3l2Z4g3m', 'Rahul Sharma', 'CSE', 2026, 8.45, 0, 'ROLE_STUDENT', TRUE, 85),
(3, 'priya.verma@jecrcu.edu.in', '$2a$10$wT6qUaQ/lF82lJk7E7p5CeG4y4vjC0Z9e3L4VqX9z9KkP3l2Z4g3m', 'Priya Verma', 'IT', 2026, 7.80, 0, 'ROLE_STUDENT', TRUE, 40),
(4, 'alumni.amit@jecrcu.edu.in', '$2a$10$wT6qUaQ/lF82lJk7E7p5CeG4y4vjC0Z9e3L4VqX9z9KkP3l2Z4g3m', 'Amit Kumar', 'CSE', 2024, 8.90, 0, 'ROLE_STUDENT', TRUE, 260),
(5, 'sneha.patel@jecrcu.edu.in', '$2a$10$wT6qUaQ/lF82lJk7E7p5CeG4y4vjC0Z9e3L4VqX9z9KkP3l2Z4g3m', 'Sneha Patel', 'ECE', 2024, 8.65, 0, 'ROLE_STUDENT', TRUE, 190),
(6, 'rohit.meena@jecrcu.edu.in', '$2a$10$wT6qUaQ/lF82lJk7E7p5CeG4y4vjC0Z9e3L4VqX9z9KkP3l2Z4g3m', 'Rohit Meena', 'ME', 2025, 7.20, 1, 'ROLE_STUDENT', TRUE, 50);

-- 2. Seed Companies (16 Companies)
INSERT INTO companies (id, name, slug, logo_url, industry, description, website, tier) VALUES
(1, 'Google', 'google', 'https://upload.wikimedia.org/wikipedia/commons/2/2f/Google_2015_logo.svg', 'Product & Tech', 'Global technology leader specializing in search, cloud computing, software, and artificial intelligence.', 'https://careers.google.com', 'Super Dream'),
(2, 'Microsoft', 'microsoft', 'https://upload.wikimedia.org/wikipedia/commons/9/96/Microsoft_logo_%282012%29.svg', 'Product & Tech', 'Worldwide leader in software, cloud computing (Azure), consumer electronics, and personal computer services.', 'https://careers.microsoft.com', 'Super Dream'),
(3, 'Amazon', 'amazon', 'https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg', 'Product & Tech', 'E-commerce, cloud computing (AWS), digital streaming, and artificial intelligence giant.', 'https://amazon.jobs', 'Super Dream'),
(4, 'Cisco Systems', 'cisco', 'https://upload.wikimedia.org/wikipedia/commons/0/08/Cisco_logo_blue_2016.svg', 'Cloud & Networking', 'Worldwide technology leader in networking hardware, software, telecommunications equipment, and cybersecurity.', 'https://jobs.cisco.com', 'Dream'),
(5, 'Zscaler', 'zscaler', 'https://upload.wikimedia.org/wikipedia/commons/e/e0/Zscaler_logo.svg', 'Cloud & Cybersecurity', 'Cloud-native cybersecurity platform delivering zero-trust exchange solutions for global enterprises.', 'https://www.zscaler.com/careers', 'Dream'),
(6, 'TCS (Tata Consultancy Services)', 'tcs', 'https://upload.wikimedia.org/wikipedia/commons/b/b1/Tata_Consultancy_Services_Logo.svg', 'IT Services', 'India''s premier IT services, consulting, and business solutions provider with Digital and Prime recruitment tracks.', 'https://www.tcs.com/careers', 'Mass & Elite'),
(7, 'Infosys', 'infosys', 'https://upload.wikimedia.org/wikipedia/commons/9/95/Infosys_logo.svg', 'IT Services', 'Global digital services and consulting firm hiring for Specialist Programmer (SP) and Digital Specialist Engineer (DSE).', 'https://www.infosys.com/careers', 'Mass & Elite'),
(8, 'Wipro', 'wipro', 'https://upload.wikimedia.org/wikipedia/commons/a/a0/Wipro_Primary_Logo_Color_RGB.svg', 'IT Services', 'Leading global information technology, consulting, and business process services company with Turbo and Elite tracks.', 'https://careers.wipro.com', 'Mass & Elite'),
(9, 'Accenture', 'accenture', 'https://upload.wikimedia.org/wikipedia/commons/c/cd/Accenture.svg', 'Consulting & IT', 'Professional services firm delivering cloud, AI, and digital transformation for Fortune Global 500 corporations.', 'https://www.accenture.com/careers', 'Mass & Elite'),
(10, 'Deloitte', 'deloitte', 'https://upload.wikimedia.org/wikipedia/commons/5/56/Deloitte.svg', 'Consulting', 'Big Four consulting and advisory network offering USI Analyst and Technology Consultant roles to engineering grads.', 'https://careers.deloitte.com', 'Dream'),
(11, 'Capgemini', 'capgemini', 'https://upload.wikimedia.org/wikipedia/commons/f/fd/Capgemini_201x_logo.svg', 'Consulting & IT', 'Global leader in partnering with companies to transform and manage their business through technology.', 'https://www.capgemini.com/careers', 'Mass & Elite'),
(12, 'Cognizant', 'cognizant', 'https://upload.wikimedia.org/wikipedia/commons/4/43/Cognizant_logo_2022.svg', 'IT Services', 'Multinational information technology services and consulting enterprise with GenC Next and GenC Elevate cadres.', 'https://careers.cognizant.com', 'Mass & Elite'),
(13, 'LTIMindtree', 'ltimindtree', 'https://upload.wikimedia.org/wikipedia/commons/7/7b/LTIMindtree_Logo.svg', 'IT Services', 'Global technology consulting and digital solutions company powering enterprise transformation.', 'https://www.ltimindtree.com/careers', 'Mass & Elite'),
(14, 'Celebal Technologies', 'celebal-technologies', 'https://celebaltech.com/assets/img/logo.png', 'Cloud & Analytics', 'Premier AI and Big Data consultancy specializing in Microsoft Azure, Databricks, and Enterprise Generative AI solutions.', 'https://celebaltech.com/careers', 'Dream'),
(15, 'Persistent Systems', 'persistent-systems', 'https://upload.wikimedia.org/wikipedia/commons/a/ae/Persistent_Systems_Logo.svg', 'Product Engineering', 'Global services and solutions company delivering digital business acceleration and enterprise modernization.', 'https://www.persistent.com/careers', 'Dream'),
(16, 'Sopra Steria', 'sopra-steria', 'https://upload.wikimedia.org/wikipedia/commons/b/b3/Sopra_Steria_logo.svg', 'IT & FinTech', 'European tech leader recognized for consulting, digital services, and software development in banking and telecom.', 'https://www.soprasteria.in/careers', 'Core & IT');

-- 3. Seed Drives (2024, 2025, 2026)
INSERT INTO drives (id, company_id, drive_year, drive_month, roles, ctc_or_stipend, min_cgpa, eligible_branches, max_backlogs, students_selected, job_type, joining_location, work_mode, bond_details, conversion_offered) VALUES
-- Google
(1, 1, 2024, 'September', 'Software Engineer (L3)', '32.50 LPA', 8.50, 'CSE, IT', 0, 2, 'PLACEMENT', 'Bangalore / Hyderabad', 'HYBRID', 'None', TRUE),
(2, 1, 2025, 'August', 'Software Engineer (L3) & Winter Intern', '35.00 LPA (Intern: 1.10 Lakh/month)', 8.50, 'CSE, IT', 0, 3, 'BOTH', 'Bangalore / Gurgaon', 'HYBRID', 'None', TRUE),
-- Microsoft
(3, 2, 2024, 'October', 'Software Development Engineer - 1', '44.00 LPA (Base: 16.5 LPA + Stocks)', 8.00, 'CSE, IT, ECE', 0, 4, 'PLACEMENT', 'Hyderabad / Noida / Bangalore', 'HYBRID', 'None', TRUE),
(4, 2, 2025, 'September', 'Software Development Engineer - 1', '48.00 LPA (Base: 17.5 LPA + Stocks)', 8.20, 'CSE, IT, ECE', 0, 5, 'PLACEMENT', 'Hyderabad / Bangalore', 'HYBRID', 'None', TRUE),
-- Amazon
(5, 3, 2024, 'August', 'Software Development Engineer (SDE-1)', '45.00 LPA (Base: 16.0 LPA + RSUs)', 7.75, 'CSE, IT, ECE, EE', 0, 6, 'PLACEMENT', 'Bangalore / Chennai / Hyderabad', 'ON_SITE', 'None', TRUE),
(6, 3, 2025, 'July', 'SDE-1 & 6-Month Intern', '45.00 LPA (Intern: 80K/month)', 7.75, 'CSE, IT, ECE', 0, 8, 'BOTH', 'Bangalore / Hyderabad', 'HYBRID', 'None', TRUE),
-- Cisco
(7, 4, 2024, 'September', 'Consulting Engineer / Technical Graduate', '18.50 LPA', 7.50, 'CSE, IT, ECE, EE', 0, 7, 'PLACEMENT', 'Bangalore', 'HYBRID', 'None', TRUE),
(8, 4, 2025, 'August', 'Software Engineer - Cloud & Security', '20.00 LPA', 7.50, 'CSE, IT, ECE', 0, 9, 'PLACEMENT', 'Bangalore', 'HYBRID', 'None', TRUE),
(9, 4, 2026, 'August', 'Software Engineer & Network Analyst', '22.00 LPA', 7.50, 'CSE, IT, ECE', 0, 10, 'PLACEMENT', 'Bangalore', 'HYBRID', 'None', TRUE),
-- Zscaler
(10, 5, 2024, 'November', 'Software Development Engineer - Cloud', '17.50 LPA', 7.50, 'CSE, IT', 0, 4, 'PLACEMENT', 'Bangalore / Mohali', 'HYBRID', 'None', TRUE),
(11, 5, 2025, 'October', 'Software Engineer - Cybersecurity', '19.00 LPA', 7.50, 'CSE, IT, ECE', 0, 5, 'PLACEMENT', 'Bangalore', 'HYBRID', 'None', TRUE),
-- TCS
(12, 6, 2024, 'July', 'TCS Prime (9 LPA) / Digital (7 LPA) / Ninja (3.6 LPA)', '3.60 - 9.00 LPA', 6.00, 'CSE, IT, ECE, EE, ME, Civil, BCA, MCA', 1, 142, 'PLACEMENT', 'Pan India', 'HYBRID', '1 Year Service Agreement', TRUE),
(13, 6, 2025, 'July', 'TCS Prime (9.5 LPA) & Digital (7.5 LPA)', '7.50 - 9.50 LPA', 6.50, 'CSE, IT, ECE, EE, MCA', 0, 88, 'PLACEMENT', 'Pan India', 'HYBRID', '1 Year Service Agreement', TRUE),
(14, 6, 2026, 'August', 'TCS National Qualifier Drive (Prime/Digital)', '7.50 - 9.50 LPA', 6.50, 'CSE, IT, ECE, EE, ME, Civil, MCA', 0, 95, 'PLACEMENT', 'Pan India', 'HYBRID', '1 Year Service Agreement', TRUE),
-- Infosys
(15, 7, 2024, 'August', 'Specialist Programmer (9.5 LPA) / DSE (6.5 LPA)', '6.50 - 9.50 LPA', 6.50, 'CSE, IT, ECE, EE, MCA', 0, 64, 'PLACEMENT', 'Bangalore / Mysore / Pune', 'HYBRID', 'None', TRUE),
(16, 7, 2025, 'August', 'Specialist Programmer (SP) & System Engineer', '4.00 - 10.00 LPA', 6.50, 'CSE, IT, ECE, EE, ME, MCA', 1, 78, 'PLACEMENT', 'Bangalore / Mysore / Pune', 'HYBRID', 'None', TRUE),
-- Wipro
(17, 8, 2024, 'September', 'Wipro Turbo (6.5 LPA) / Elite (3.5 LPA)', '3.50 - 6.50 LPA', 6.00, 'CSE, IT, ECE, EE, ME, Civil, MCA', 1, 52, 'PLACEMENT', 'Hyderabad / Chennai / Pune', 'HYBRID', '1 Year Service Agreement', TRUE),
(18, 8, 2025, 'September', 'Wipro Turbo SDE', '7.00 LPA', 6.50, 'CSE, IT, ECE, MCA', 0, 38, 'PLACEMENT', 'Bangalore / Pune', 'HYBRID', '1 Year Service Agreement', TRUE),
-- Accenture
(19, 9, 2024, 'August', 'Advanced ASE (11.0 LPA) / ASE (4.5 LPA)', '4.50 - 11.00 LPA', 6.50, 'CSE, IT, ECE, EE, ME, Civil, MCA', 0, 110, 'PLACEMENT', 'Gurgaon / Noida / Bangalore', 'HYBRID', 'None', TRUE),
(20, 9, 2025, 'August', 'Full Stack Engineering Associate (12.0 LPA)', '4.80 - 12.00 LPA', 6.50, 'CSE, IT, ECE, MCA', 0, 125, 'PLACEMENT', 'Bangalore / Gurgaon / Pune', 'HYBRID', 'None', TRUE),
-- Deloitte
(21, 10, 2024, 'September', 'Analyst - Technology Consulting (USI)', '8.50 LPA', 7.00, 'CSE, IT, ECE', 0, 28, 'PLACEMENT', 'Hyderabad / Bangalore / Gurgaon', 'HYBRID', 'None', TRUE),
(22, 10, 2025, 'September', 'Associate Analyst & Advisory Consultant', '9.20 LPA', 7.00, 'CSE, IT, ECE, EE', 0, 32, 'PLACEMENT', 'Hyderabad / Gurgaon', 'HYBRID', 'None', TRUE),
(23, 10, 2026, 'September', 'USI Analyst - Cyber & Cloud', '9.50 LPA', 7.00, 'CSE, IT, ECE', 0, 35, 'PLACEMENT', 'Hyderabad / Bangalore', 'HYBRID', 'None', TRUE),
-- Capgemini
(24, 11, 2024, 'October', 'Senior Analyst (7.5 LPA) / Analyst (4.25 LPA)', '4.25 - 7.50 LPA', 6.25, 'CSE, IT, ECE, EE, MCA', 0, 48, 'PLACEMENT', 'Noida / Pune / Mumbai', 'HYBRID', 'None', TRUE),
(25, 11, 2025, 'September', 'Senior Analyst - Cloud Infrastructure', '8.00 LPA', 6.50, 'CSE, IT, ECE, MCA', 0, 42, 'PLACEMENT', 'Pune / Bangalore', 'HYBRID', 'None', TRUE),
-- Cognizant
(26, 12, 2024, 'October', 'GenC Elevate (6.75 LPA) / GenC (4.0 LPA)', '4.00 - 6.75 LPA', 6.50, 'CSE, IT, ECE, EE, MCA', 0, 56, 'PLACEMENT', 'Coimbatore / Chennai / Pune', 'HYBRID', 'None', TRUE),
(27, 12, 2025, 'October', 'GenC Pro Full Stack Developer', '7.25 LPA', 6.50, 'CSE, IT, ECE, MCA', 0, 60, 'PLACEMENT', 'Chennai / Bangalore', 'HYBRID', 'None', TRUE),
-- LTIMindtree
(28, 13, 2024, 'November', 'Graduate Trainee - Edge Track', '6.50 LPA', 6.50, 'CSE, IT, ECE', 0, 34, 'PLACEMENT', 'Mumbai / Pune / Chennai', 'HYBRID', 'None', TRUE),
(29, 13, 2025, 'November', 'Software Engineer - Enterprise Applications', '7.20 LPA', 6.50, 'CSE, IT, ECE, MCA', 0, 40, 'PLACEMENT', 'Pune / Bangalore', 'HYBRID', 'None', TRUE),
-- Celebal Technologies
(30, 14, 2024, 'December', 'Associate Cloud & AI Engineer', '6.50 LPA', 6.50, 'CSE, IT, ECE, MCA', 0, 45, 'BOTH', 'Jaipur / Noida / Bangalore', 'ON_SITE', '1 Year Agreement', TRUE),
(31, 14, 2025, 'November', 'Databricks & Generative AI Associate', '7.50 LPA', 6.75, 'CSE, IT, ECE, MCA', 0, 52, 'BOTH', 'Jaipur / Noida', 'HYBRID', '1 Year Agreement', TRUE),
(32, 14, 2026, 'October', 'AI/ML Cloud Solutions Engineer', '8.50 LPA', 7.00, 'CSE, IT, ECE, MCA', 0, 55, 'BOTH', 'Jaipur / Bangalore', 'HYBRID', '1 Year Agreement', TRUE),
-- Persistent Systems
(33, 15, 2024, 'November', 'Software Engineer Trainee', '8.00 LPA', 7.00, 'CSE, IT, ECE', 0, 18, 'PLACEMENT', 'Pune / Goa / Bangalore', 'HYBRID', 'None', TRUE),
(34, 15, 2025, 'October', 'Software Engineer Trainee', '9.00 LPA', 7.00, 'CSE, IT, ECE', 0, 22, 'PLACEMENT', 'Pune / Bangalore', 'HYBRID', 'None', TRUE),
-- Sopra Steria
(35, 16, 2024, 'December', 'Graduate Engineer Trainee', '6.00 LPA', 6.50, 'CSE, IT, ECE, MCA', 0, 25, 'PLACEMENT', 'Noida / Chennai', 'HYBRID', 'None', TRUE),
(36, 16, 2025, 'December', 'Associate Software Engineer', '6.50 LPA', 6.50, 'CSE, IT, ECE, MCA', 0, 28, 'PLACEMENT', 'Noida / Bangalore', 'HYBRID', 'None', TRUE);

-- 4. Seed Selection Rounds
INSERT INTO rounds (id, drive_id, round_number, name, round_type, duration_minutes, difficulty, description) VALUES
-- Google 2025 (Drive 2)
(1, 2, 1, 'Online Assessment (Google OA)', 'ONLINE_ASSESSMENT', 90, 'HARD', '2 Algorithmic coding problems on Google internal platform. Emphasis on dynamic programming, trees, and graphs.'),
(2, 2, 2, 'Technical Interview 1 (Data Structures)', 'TECHNICAL', 45, 'HARD', 'Live 1-on-1 coding in Google Docs. Graph algorithms, shortest paths, and complexity trade-offs.'),
(3, 2, 3, 'Technical Interview 2 (Algorithms & Problem Solving)', 'TECHNICAL', 45, 'HARD', 'Advanced DP, bit manipulation, and code testability.'),
(4, 2, 4, 'Googliness & Leadership (Fitment)', 'HR', 45, 'MEDIUM', 'Behavioral scenarios, collaboration, dealing with ambiguity, ethics, and innovation.'),
-- Microsoft 2025 (Drive 4)
(5, 4, 1, 'Online Coding Assessment (Codility)', 'CODING', 75, 'MEDIUM', '3 coding challenges covering array manipulation, binary search, and prefix sums.'),
(6, 4, 2, 'Technical Round 1 (DSA & Core CS)', 'TECHNICAL', 60, 'MEDIUM', 'Tree traversals, recursion, and operating systems memory management.'),
(7, 4, 3, 'Technical Round 2 (System Design & Projects)', 'TECHNICAL', 60, 'HARD', 'Low-level design of real-time notification system and deep dive into resume projects.'),
(8, 4, 4, 'AA (As Appropriate) / Director Round', 'HR', 45, 'MEDIUM', 'Core cultural alignment, growth mindset, and behavioral questions.'),
-- Amazon 2025 (Drive 6)
(9, 6, 1, 'Amazon Online Assessment (OA2)', 'ONLINE_ASSESSMENT', 90, 'MEDIUM', '2 coding questions + Work Style Assessment (Amazon 16 Leadership Principles).'),
(10, 6, 2, 'Technical Round 1 (Problem Solving & DSA)', 'TECHNICAL', 60, 'MEDIUM', 'Binary Trees, Graphs (BFS/DFS), and questions evaluated against Customer Obsession.'),
(11, 6, 3, 'Technical Round 2 (Data Structures & OOP)', 'TECHNICAL', 60, 'HARD', 'LRU Cache implementation, multithreading, and OOP design patterns.'),
(12, 6, 4, 'Bar Raiser Round', 'HR', 60, 'HARD', 'System scalability and deep dive into Deliver Results and Bias for Action principles.'),
-- Cisco 2026 (Drive 9)
(13, 9, 1, 'Aptitude & Technical MCQ Assessment', 'APTITUDE', 60, 'MEDIUM', 'Quantitative aptitude, logical reasoning, and C/C++ networking MCQs.'),
(14, 9, 2, 'Technical Round 1 (Networking & OS)', 'TECHNICAL', 60, 'MEDIUM', 'OSI model, TCP vs UDP, subnetting, and process synchronization.'),
(15, 9, 3, 'Managerial & HR Interview', 'HR', 45, 'EASY', 'Discussion on aspirations, adaptability, and relocation preferences.'),
-- TCS 2025 (Drive 13)
(16, 13, 1, 'National Qualifier Test (NQT)', 'ONLINE_ASSESSMENT', 120, 'MEDIUM', 'Cognitive skills + advanced coding section (2 problems) for Prime/Digital evaluation.'),
(17, 13, 2, 'Technical Interview', 'TECHNICAL', 40, 'MEDIUM', 'SQL joins, Java collections, OOP principles, and final year capstone project.'),
(18, 13, 3, 'Managerial & HR Round', 'HR', 25, 'EASY', 'Company awareness, willing to work in shifts, relocation, and background verification.'),
-- Deloitte 2026 (Drive 23)
(19, 23, 1, 'Cognitive Ability & Coding Assessment', 'ONLINE_ASSESSMENT', 75, 'MEDIUM', 'Verbal reasoning, critical thinking, pseudo-code, and 1 medium programming question.'),
(20, 23, 2, 'Case Study & Technical Interview', 'CASE_STUDY', 45, 'MEDIUM', 'Client scenario analysis: proposing a cloud migration architecture for retail banking.'),
(21, 23, 3, 'Partner Interview', 'HR', 30, 'MEDIUM', 'Consulting mindset, business ethics, executive communication, and team leadership.'),
-- Celebal Technologies 2026 (Drive 32)
(22, 32, 1, 'Aptitude & Python / SQL Assessment', 'ONLINE_ASSESSMENT', 60, 'MEDIUM', 'Python data structures, SQL queries (aggregations, window functions), and logic.'),
(23, 32, 2, 'Technical Interview (Cloud & Databases)', 'TECHNICAL', 45, 'MEDIUM', 'Cloud fundamentals, data pipelines, SQL indexing, and project architecture.'),
(24, 32, 3, 'HR & Fitment Round', 'HR', 30, 'EASY', 'Jaipur office culture, willingness to learn Azure/Databricks, communication.');

-- 5. Seed Interview Questions
INSERT INTO questions (id, round_id, question_text, topic_tags, difficulty, answer_or_hint) VALUES
-- Google Round 1 (Drive 2, Round 1)
(1, 1, 'Given an array of integers representing house values, find the maximum money you can rob without robbing two adjacent houses (House Robber II on a circular street).', 'DSA, Dynamic Programming', 'MEDIUM', 'Use DP. Because street is circular, compute max of: rob(0 to N-2) and rob(1 to N-1). O(N) time and O(1) space.'),
(2, 1, 'Given a directed graph with N vertices and M weighted edges, find the shortest path from vertex S to all other vertices. If negative cycle exists, detect and report it.', 'DSA, Graphs', 'HARD', 'Implement Bellman-Ford algorithm with relaxation for V-1 passes, then 1 extra pass to detect negative cycle. O(V*E) time complexity.'),
-- Google Round 2 (Drive 2, Round 2)
(3, 2, 'Given a stream of integers arriving continuously, design a data structure that returns the median value of all numbers seen so far in O(1) time.', 'DSA, Heaps', 'HARD', 'Maintain two heaps: a max-heap for lower half and a min-heap for upper half. Keep sizes balanced (difference <= 1). Insertion is O(log N), median query is O(1).'),
(4, 2, 'Explain the difference between a process and a thread. How does the Linux kernel represent them under the hood?', 'OS, Process Management', 'MEDIUM', 'In Linux, both are task_struct instances created via clone() syscall. Threads share address space and file descriptors (CLONE_VM, CLONE_FS).'),
-- Google Round 4 (Googliness)
(5, 4, 'Describe a time when you strongly disagreed with a team member on an engineering design decision. How did you resolve it constructively?', 'HR, Behavioral', 'MEDIUM', 'Use the STAR method (Situation, Task, Action, Result). Focus on data-driven benchmarking and putting team goals above ego.'),
-- Microsoft Round 1 (Drive 4, Round 1)
(6, 5, 'Given a sorted array that has been rotated at an unknown pivot, search for a target value in O(log N) time.', 'DSA, Binary Search', 'MEDIUM', 'Modified binary search: determine which half (left or right) is sorted, then check if target lies within the sorted range to narrow search boundaries.'),
(7, 5, 'Find the largest contiguous sub-array sum in an array containing both positive and negative integers.', 'DSA, Kadane Algorithm', 'EASY', 'Use Kadane''s algorithm: maintain max_so_far and current_max. Reset current_max to 0 if it dips below zero. O(N) time, O(1) space.'),
-- Microsoft Round 2 (Drive 4, Round 2)
(8, 6, 'Implement an LRU (Least Recently Used) Cache with get(key) and put(key, value) operations running in strictly O(1) time complexity.', 'DSA, Hash Table, Doubly Linked List', 'HARD', 'Combine a HashMap for O(1) lookup with a Doubly Linked List for O(1) node detachment and insertion at head (MRU position).'),
(9, 6, 'What is the difference between Clustered Index and Non-Clustered Index in relational databases like PostgreSQL/SQL Server?', 'DBMS, Indexing', 'MEDIUM', 'Clustered index determines physical ordering of table rows (only 1 per table). Non-clustered index stores a separate B-tree pointer to the physical data rows.'),
-- Microsoft Round 3 (Drive 4, Round 3)
(10, 7, 'Design a scalable URL Shortening service like TinyURL. What database schema, hashing mechanism, and caching layer would you propose for 100M daily writes?', 'System Design, Architecture', 'HARD', 'Use Base62 encoding on unique 64-bit ID from Snowflake counter, Redis cache for hot redirects (90% read traffic), and NoSQL/PostgreSQL partitioned by hash range.'),
-- Amazon Round 1 (Drive 6, Round 1)
(11, 9, 'Given an elevation map where width of each bar is 1, compute how much water it can trap after raining.', 'DSA, Two Pointers', 'HARD', 'Use two pointers (left and right) with left_max and right_max trackers. Move the pointer with smaller height. O(N) time, O(1) space.'),
(12, 9, 'Given a string containing just characters ''('', '')'', ''{'', ''}'', ''['' and '']'', determine if the input string is valid.', 'DSA, Stack', 'EASY', 'Push opening brackets to stack. On closing bracket, verify that stack is non-empty and top element matches the corresponding pair. O(N) time and space.'),
-- Amazon Round 2 (Drive 6, Round 2)
(13, 10, 'Find the Lowest Common Ancestor (LCA) of two given nodes in a Binary Tree (not necessarily a BST).', 'DSA, Trees', 'MEDIUM', 'Use recursive DFS. If current node matches p or q, return current node. Recurse left and right; if both sides return non-null, current node is the LCA.'),
(14, 10, 'Explain the ACID properties of database transactions. Provide a real-world scenario where Isolation level prevents a Dirty Read.', 'DBMS, Transactions', 'MEDIUM', 'Atomicity, Consistency, Isolation, Durability. Read Committed prevents reading uncommitted changes written by concurrent transactions.'),
-- Amazon Round 3 (Drive 6, Round 3)
(15, 11, 'Design an in-memory Parking Lot system using Object Oriented Design principles (Java classes, interfaces, and SOLID principles).', 'OOP, Low Level Design', 'MEDIUM', 'Entities: ParkingLot, Level, ParkingSpot (Compact, Large, Handicapped), Vehicle (Car, Bike, Bus), Ticket, Payment. Use Strategy pattern for fee calculation.'),
-- Cisco Round 2 (Drive 9, Round 2)
(16, 14, 'Walk through the step-by-step process of what happens when you type https://www.google.com into your browser and press Enter.', 'Computer Networks, Web Protocols', 'MEDIUM', 'DNS lookup (browser cache -> OS -> resolver -> root -> TLD -> authoritative), TCP 3-way handshake, TLS 1.3 handshake, HTTP GET request, server response, browser rendering DOM/CSSOM.'),
(17, 14, 'What is the difference between TCP and UDP? Why does DNS use UDP on port 53, and when does it fall back to TCP?', 'Computer Networks, Transport Layer', 'MEDIUM', 'TCP is connection-oriented with guaranteed delivery and flow control. UDP is connectionless with low overhead. DNS uses UDP for fast queries (< 512 bytes) and falls back to TCP for zone transfers or large responses.'),
-- TCS Round 2 (Drive 13, Round 2)
(18, 17, 'What is the difference between String, StringBuilder, and StringBuffer in Java? When should each be chosen?', 'Java, Core Language', 'EASY', 'String is immutable (stored in String Pool). StringBuilder is mutable and not thread-safe (fastest for single-thread loops). StringBuffer is mutable and synchronized/thread-safe.'),
(19, 17, 'Write a SQL query to find the 3rd highest salary from an Employee table without using the LIMIT or TOP keyword.', 'DBMS, SQL', 'MEDIUM', 'SELECT salary FROM (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) as rank_no FROM Employee) WHERE rank_no = 3;'),
-- Deloitte Round 2 (Drive 23, Round 2)
(20, 20, 'How would you architect a secure microservices backend handling digital payment transactions for high availability and zero downtime?', 'System Design, Cloud & Security', 'MEDIUM', 'Use API Gateway, OAuth2/JWT authentication, distributed tracing with OpenTelemetry, event-driven architecture using Kafka, and database replication across multiple availability zones.'),
-- Celebal Technologies Round 2 (Drive 32, Round 2)
(21, 23, 'Explain how a Spark distributed dataframe partitions data across worker nodes, and what causes an expensive data shuffle during joins.', 'Big Data, Analytics', 'MEDIUM', 'Data is partitioned across cluster executors. Operations like groupBy or wide joins require repartitioning data across the network (shuffle), which introduces disk I/O and network latency.');

-- 6. Seed Approved Experiences
INSERT INTO experiences (id, drive_id, author_id, is_anonymous, title, content, tips, mistakes_to_avoid, outcome, difficulty_rating, status, upvotes) VALUES
(1, 4, 4, FALSE, 'My Journey to Microsoft IDC - SDE-1 Selection Experience',
 'The recruitment drive began with a 75-minute Codility test. All 3 problems were solvable if you are comfortable with LeetCode Medium (Array, HashMaps, and Binary Search). Round 1 was focused on Tree algorithms and OS concepts. Round 2 went deep into System Design and my open-source project. The final Director round was surprisingly conversational—focused on growth mindset and why Microsoft.',
 '1. Practice explaining your code aloud while typing.\n2. Master Tree traversals and Graph BFS/DFS.\n3. Be thorough with every single bullet point on your resume.',
 'Do not jump straight into coding before confirming edge cases (null inputs, duplicate numbers, integer overflow).',
 'SELECTED', 4, 'APPROVED', 42),

(2, 6, 5, FALSE, 'Cracking Amazon SDE-1 On-Campus at JECRC University',
 'Amazon’s drive was intense and fast-paced. The OA had 2 algorithmic problems plus 20 behavioral questions that mapped to the 16 Leadership Principles. In technical rounds, every question had 10-15 minutes dedicated to LP questions ("Tell me about a time you had a tight deadline"). For the coding question on Two Pointers, writing clean, modular code with descriptive variable names made all the difference.',
 '1. Prepare 2 STAR-method stories for each Amazon Leadership Principle.\n2. Dry-run your solution with test cases before saying you are done.\n3. Communicate your thought process clearly.',
 'Never fake project details or metrics on your resume; the interviewers will ask for deep architectural details.',
 'SELECTED', 4, 'APPROVED', 35),

(3, 13, 6, FALSE, 'From Mech to TCS Digital Offer - Preparation Strategy',
 'Coming from Mechanical Engineering, I focused 6 months on Java, Object-Oriented Programming, and SQL. In the NQT exam, clearing the advanced coding section unlocked the TCS Digital interview. The technical panel asked about SQL Joins, Collections, and why I wanted to transition into IT. Being confident and showing real passion for software development was key.',
 '1. Non-CSE students should build at least one full-stack or backend web application.\n2. Master SQL queries (Joins, Aggregations, Group By).\n3. Keep your fundamentals solid.',
 'Do not panic if you do not know a specific framework; admit it honestly and explain how you would figure it out.',
 'SELECTED', 3, 'APPROVED', 28),

(4, 2, 2, TRUE, 'Google Campus Interview Experience (Winter Internship)',
 'The online assessment questions were very interesting—neither was a standard textbook question. One involved calculating shortest paths on a graph with state transitions. In the technical round, the interviewer helped me when I got stuck on a DP optimization. Writing bug-free code in a blank Google Doc requires good practice without IDE autocompletion.',
 '1. Practice writing code on paper or in plain text documents without syntax highlighting.\n2. Analyze time and space complexity with precision.\n3. Listen closely to interviewer hints.',
 'Avoid silence. An interview is a collaborative dialogue, not a silent exam.',
 'SELECTED', 5, 'APPROVED', 51);

-- 7. Seed Difficulty Votes
INSERT INTO difficulty_votes (company_id, user_id, rating) VALUES
(1, 2, 5), (1, 4, 5), (1, 5, 4),
(2, 4, 4), (2, 2, 4), (2, 3, 4),
(3, 5, 4), (3, 4, 4), (3, 6, 4),
(4, 4, 4), (4, 5, 3),
(6, 6, 3), (6, 3, 3), (6, 2, 2),
(10, 3, 3), (10, 4, 4),
(14, 2, 3), (14, 6, 3);

-- 8. Seed Alumni Profiles (Mentors for juniors)
INSERT INTO alumni_profiles (id, user_id, company_name, job_role, graduation_year, branch, linkedin_url, topics_can_help, visible) VALUES
(1, 4, 'Microsoft', 'Software Development Engineer - 1', 2024, 'CSE', 'https://linkedin.com/in/amit-kumar-jecrc', 'DSA, System Design, Microsoft Interview Prep, Resume Review', TRUE),
(2, 5, 'Amazon', 'SDE-1', 2024, 'ECE', 'https://linkedin.com/in/sneha-patel-amazon', 'Amazon Leadership Principles, Low Level Design, DSA Trees & Graphs', TRUE),
(3, 6, 'TCS Digital', 'Systems Engineer (Full Stack)', 2025, 'ME', 'https://linkedin.com/in/rohit-meena-tcs', 'Non-CS to Tech Transition, Core Java, SQL, Capstone Projects', TRUE);

-- 9. Seed Upcoming Recruitment Drives
INSERT INTO upcoming_drives (id, company_id, company_name, drive_date, roles_offered, min_cgpa, eligible_branches, max_backlogs, ctc_or_stipend, registration_link, deadline, active) VALUES
(1, 1, 'Google', '2026-11-10', 'Software Engineer (L3) & Summer Intern', 8.00, 'CSE, IT', 0, '35.00 LPA (Intern: 1.15 Lakh/mo)', 'https://forms.gle/jecrc-google-2026', '2026-11-05 23:59:59', TRUE),
(2, 2, 'Microsoft', '2026-10-28', 'Software Development Engineer - 1', 7.50, 'CSE, IT, ECE', 0, '48.00 LPA', 'https://forms.gle/jecrc-msft-2026', '2026-10-25 18:00:00', TRUE),
(3, 10, 'Deloitte', '2026-11-15', 'Analyst - Technology Consulting (USI)', 6.75, 'CSE, IT, ECE, EE', 0, '9.50 LPA', 'https://forms.gle/jecrc-deloitte-2026', '2026-11-10 17:00:00', TRUE),
(4, 14, 'Celebal Technologies', '2026-10-22', 'Cloud & Generative AI Associate', 6.50, 'CSE, IT, ECE, MCA', 0, '8.50 LPA', 'https://forms.gle/jecrc-celebal-2026', '2026-10-20 23:59:59', TRUE);

-- 10. Seed Badges
INSERT INTO badges (id, code, title, description, icon) VALUES
(1, 'FIRST_CONTRIBUTOR', 'First Contributor', 'Submitted your first placement interview experience', 'bi-award'),
(2, 'HELPFUL_50', 'Helpful Senior', 'Earned 50 or more upvotes across shared experiences', 'bi-heart-fill'),
(3, 'MENTOR', 'Verified Mentor', 'Completed 3+ mock interviews with junior students', 'bi-people-fill'),
(4, 'COMMUNITY_STAR', 'Community Star', 'Active contributor answering questions in company discussions', 'bi-star-fill');

-- 11. Award User Badges
INSERT INTO user_badges (user_id, badge_id) VALUES
(4, 1), (4, 2),
(5, 1),
(6, 1);

-- 12. Seed Bookmarks for test users
INSERT INTO bookmarks (user_id, item_type, item_id) VALUES
(2, 'COMPANY', 1),
(2, 'COMPANY', 2),
(2, 'QUESTION', 1),
(2, 'QUESTION', 8);

-- 13. Seed Practice Attempts
INSERT INTO practice_attempts (user_id, question_id, status, notes) VALUES
(2, 6, 'SOLVED', 'Solved using binary search on rotated sorted array in 25 mins.'),
(2, 7, 'SOLVED', 'Kadane algorithm implemented with standard O(N) time.'),
(2, 8, 'UNSURE', 'Need to review doubly linked list node pointer reconnection.'),
(2, 1, 'SKIPPED', 'Need to revise DP state transition for circular arrays.');

-- 14. Seed Company Comments / Q&A
INSERT INTO comments (id, company_id, author_id, parent_id, content, status) VALUES
(1, 2, 2, NULL, 'Does Microsoft allow Python or Java for the coding assessment, or is C++ mandatory?', 'VISIBLE'),
(2, 2, 4, 1, 'Yes! You can use Python, Java, C++, or C# without any penalty. Choose whichever language you are most fluent in.', 'VISIBLE'),
(3, 1, 3, NULL, 'For the Google OA, are 3-D dynamic programming questions asked or mostly graph problems?', 'VISIBLE'),
(4, 1, 4, 3, 'In our batch it was mostly Graph BFS/DFS and 1-D / 2-D DP. 3-D DP is very rare for campus rounds.', 'VISIBLE');

-- 15. Seed Notification Preferences
INSERT INTO notification_preferences (user_id, email_drives, email_deadlines, email_experiences, email_bookings) VALUES
(1, TRUE, TRUE, TRUE, TRUE),
(2, TRUE, TRUE, TRUE, TRUE),
(3, TRUE, TRUE, TRUE, TRUE),
(4, TRUE, TRUE, TRUE, TRUE),
(5, TRUE, TRUE, TRUE, TRUE),
(6, TRUE, TRUE, TRUE, TRUE);

-- 16. Seed Notifications
INSERT INTO notifications (user_id, title, message, link, notification_type, is_read) VALUES
(2, 'New Upcoming Drive: Microsoft', 'Microsoft SDE-1 recruitment drive announced. Registration closes on Oct 25.', '/upcoming', 'UPCOMING_DRIVE', FALSE),
(2, 'Upcoming Drive: Celebal Technologies', 'Celebal Technologies Cloud & AI drive scheduled. You are eligible!', '/upcoming', 'UPCOMING_DRIVE', FALSE),
(2, 'Experience Approved', 'Your recent question received a verified answer from senior Amit Kumar.', '/companies/2', 'NEW_EXPERIENCE', TRUE);

-- 17. Seed Mock Slots
INSERT INTO mock_slots (id, mentor_id, company_name, start_time, end_time, status) VALUES
(1, 4, 'Microsoft', '2026-10-22 17:00:00', '2026-10-22 18:00:00', 'AVAILABLE'),
(2, 4, 'Google', '2026-10-23 18:00:00', '2026-10-23 19:00:00', 'AVAILABLE'),
(3, 5, 'Amazon', '2026-10-24 16:00:00', '2026-10-24 17:00:00', 'AVAILABLE');
