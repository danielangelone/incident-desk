insert into application (name, owner_team) values
    ('payments-api',   'payments'),
    ('auth-service',   'platform'),
    ('checkout-web',   'frontend');

insert into server (hostname, environment) values
    ('prod-web-01',     'PROD'),
    ('prod-web-02',     'PROD'),
    ('staging-web-01',  'STAGING');

insert into incident (title, severity, status, application_id, server_id, started_at, resolved_at, root_cause) values
    ('Payment gateway timeouts',      'HIGH',     'RESOLVED', 1, 1, now() - interval '5 days', now() - interval '4 days 22 hours', 'Third-party gateway rate-limited our IPs; rotated egress IPs.'),
    ('Auth tokens expiring too early', 'MEDIUM',   'RESOLVED', 2, null, now() - interval '3 days', now() - interval '2 days 20 hours', 'Clock skew on auth-service pod; added NTP sync to deployment.'),
    ('Checkout page 500s on mobile',   'HIGH',     'INVESTIGATING', 3, 2, now() - interval '2 hours', null, null),
    ('Elevated error rate on login',   'LOW',      'OPEN',         2, null, now() - interval '30 minutes', null, null);

insert into incident_note (incident_id, body, created_at) values
    (1, 'Seeing 60% timeout rate on /v1/charge.', now() - interval '5 days'),
    (1, 'Switched to backup gateway, errors clearing.', now() - interval '4 days 23 hours'),
    (3, 'Only affects Safari on iOS 17. Investigating CSP headers.', now() - interval '90 minutes'),
    (3, 'Found null-pointer in cart serializer when item has no image URL.', now() - interval '45 minutes');
