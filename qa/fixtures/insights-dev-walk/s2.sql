PRAGMA foreign_keys=ON;
BEGIN;
DELETE FROM attack WHERE id NOT IN ('seed-tokyo','seed-night','851c4ee6-8923-4f5d-9528-dae891bd743b');
COMMIT;
SELECT count(*) FROM attack; SELECT count(*) FROM intensity_point; SELECT count(*) FROM attack_tag;
