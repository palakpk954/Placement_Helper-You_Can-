-- Flyway Migration V6: Use reliable local SVG vectors for company logos
UPDATE companies SET logo_url = '/img/companies/google.svg' WHERE slug = 'google' OR id = 1;
UPDATE companies SET logo_url = '/img/companies/microsoft.svg' WHERE slug = 'microsoft' OR id = 2;
UPDATE companies SET logo_url = '/img/companies/amazon.svg' WHERE slug = 'amazon' OR id = 3;
UPDATE companies SET logo_url = '/img/companies/cisco.svg' WHERE slug = 'cisco' OR id = 4;
UPDATE companies SET logo_url = '/img/companies/zscaler.svg' WHERE slug = 'zscaler' OR id = 5;
UPDATE companies SET logo_url = '/img/companies/tcs.svg' WHERE slug = 'tcs' OR id = 6;
UPDATE companies SET logo_url = '/img/companies/infosys.svg' WHERE slug = 'infosys' OR id = 7;
UPDATE companies SET logo_url = '/img/companies/wipro.svg' WHERE slug = 'wipro' OR id = 8;
UPDATE companies SET logo_url = '/img/companies/accenture.svg' WHERE slug = 'accenture' OR id = 9;
UPDATE companies SET logo_url = '/img/companies/deloitte.svg' WHERE slug = 'deloitte' OR id = 10;
UPDATE companies SET logo_url = '/img/companies/capgemini.svg' WHERE slug = 'capgemini' OR id = 11;
UPDATE companies SET logo_url = '/img/companies/cognizant.svg' WHERE slug = 'cognizant' OR id = 12;
UPDATE companies SET logo_url = '/img/companies/ltimindtree.svg' WHERE slug = 'ltimindtree' OR id = 13;
UPDATE companies SET logo_url = '/img/companies/celebal.svg' WHERE slug = 'celebal-technologies' OR slug = 'celebal' OR id = 14;
UPDATE companies SET logo_url = '/img/companies/persistent.svg' WHERE slug = 'persistent-systems' OR slug = 'persistent' OR id = 15;
UPDATE companies SET logo_url = '/img/companies/sopra-steria.svg' WHERE slug = 'sopra-steria' OR id = 16;
