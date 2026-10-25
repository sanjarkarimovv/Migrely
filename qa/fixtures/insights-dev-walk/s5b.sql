DELETE FROM day_mark WHERE date >= '2026-09-01';
SELECT group_concat(date) FROM day_mark;
