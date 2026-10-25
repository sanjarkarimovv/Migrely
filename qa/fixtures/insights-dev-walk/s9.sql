UPDATE attack SET start_zone_id='Mars/Olympus' WHERE substr(id,-2)='12';
SELECT substr(id,-2), start_zone_id FROM attack WHERE start_zone_id NOT LIKE 'Asia/%';
