INSERT INTO attack_tag SELECT id,'TRIGGER','period' FROM attack WHERE substr(id,-2) IN ('05','07','08','12');
SELECT tag_key,count(*) FROM attack_tag GROUP BY tag_key;
