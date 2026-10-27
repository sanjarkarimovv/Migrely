PRAGMA foreign_keys=ON;
BEGIN;
DELETE FROM medication_intake;
DELETE FROM medication;
INSERT INTO medication VALUES
 ('c2200000-0000-4000-8000-000000000001','Sumatriptan','sumatriptan','ACUTE','TRIPTAN','50 mg',0,1751770000000,1751770000000),
 ('c2200000-0000-4000-8000-000000000002','Ibuprofen','ibuprofen','ACUTE','SIMPLE','400 mg',0,1751770000001,1751770000001),
 ('c2200000-0000-4000-8000-000000000003','Topiramate','topiramate','PREVENTIVE',NULL,'25 mg',0,1751770000002,1751770000002);
INSERT INTO medication_intake(id,medication_id,attack_id,taken_epoch_millis,taken_zone_id,dose,created_epoch_millis,effect,effect_closed)
 SELECT 'c2200000-0000-4000-8000-0000000001'||substr(a.id,-2), 'c2200000-0000-4000-8000-000000000001', a.id,
        a.start_epoch_millis+3600000, 'Asia/Tashkent', '50 mg', a.start_epoch_millis+3600000,
        CASE substr(a.id,-2) WHEN '01' THEN 'A_LOT' WHEN '03' THEN 'A_LOT' WHEN '05' THEN 'COMPLETELY' ELSE 'NOT_AT_ALL' END, 1
 FROM attack a WHERE substr(a.id,-2) IN ('01','03','05','08');
INSERT INTO medication_intake(id,medication_id,attack_id,taken_epoch_millis,taken_zone_id,dose,created_epoch_millis,effect,effect_closed)
 SELECT 'c2200000-0000-4000-8000-0000000002'||substr(a.id,-2), 'c2200000-0000-4000-8000-000000000002', a.id,
        a.start_epoch_millis+3600000, 'Asia/Tashkent', '400 mg', a.start_epoch_millis+3600000,
        CASE substr(a.id,-2) WHEN '10' THEN 'A_LOT' WHEN '12' THEN 'NOT_AT_ALL' ELSE NULL END,
        CASE substr(a.id,-2) WHEN '11' THEN 0 ELSE 1 END
 FROM attack a WHERE substr(a.id,-2) IN ('10','11','12');
INSERT INTO medication_intake VALUES ('c2200000-0000-4000-8000-000000000301','c2200000-0000-4000-8000-000000000003',NULL,
 (CAST(strftime('%s','2026-10-20 08:00','-5 hours') AS INTEGER)*1000),'Asia/Tashkent','25 mg',1792000000000,NULL,0);
COMMIT;
SELECT m.name, datetime(i.taken_epoch_millis/1000,'unixepoch','+5 hours'), i.effect, i.effect_closed, substr(i.attack_id,-2)
 FROM medication_intake i JOIN medication m ON m.id=i.medication_id ORDER BY i.taken_epoch_millis;
