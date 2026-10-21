-- Flyway Migration V7: Update remaining partner companies with local SVG vector logos
UPDATE companies SET logo_url = '/img/companies/servicenow.svg' WHERE slug = 'service-now' OR id = 17;
UPDATE companies SET logo_url = '/img/companies/hpe.svg' WHERE slug = 'hpe' OR id = 18;
UPDATE companies SET logo_url = '/img/companies/autodesk.svg' WHERE slug = 'autodesk' OR id = 19;
UPDATE companies SET logo_url = '/img/companies/commvault.svg' WHERE slug = 'commvault' OR id = 20;
UPDATE companies SET logo_url = '/img/companies/atlassian.svg' WHERE slug = 'atlassian' OR id = 21;
UPDATE companies SET logo_url = '/img/companies/walmart.svg' WHERE slug = 'walmart-global-tech' OR id = 22;
UPDATE companies SET logo_url = '/img/companies/flipkart.svg' WHERE slug = 'flipkart' OR id = 23;
UPDATE companies SET logo_url = '/img/companies/zs.svg' WHERE slug = 'zs-associates' OR id = 25;
UPDATE companies SET logo_url = '/img/companies/finastra.svg' WHERE slug = 'finastra' OR id = 27;
UPDATE companies SET logo_url = '/img/companies/blinkit.svg' WHERE slug = 'blinkit' OR id = 30;
UPDATE companies SET logo_url = '/img/companies/zomato.svg' WHERE slug = 'zomato' OR id = 31;
UPDATE companies SET logo_url = '/img/companies/cyntexa.svg' WHERE slug = 'cyntexa' OR id = 37;
UPDATE companies SET logo_url = '/img/companies/coforge.svg' WHERE slug = 'coforge' OR id = 38;
UPDATE companies SET logo_url = '/img/companies/wayfair.svg' WHERE slug = 'wayfair' OR id = 40;
