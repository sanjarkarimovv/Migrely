BEGIN;
UPDATE attack SET start_epoch_millis=start_epoch_millis+172800000, end_epoch_millis=end_epoch_millis+172800000, created_epoch_millis=created_epoch_millis+172800000, updated_epoch_millis=updated_epoch_millis+172800000 WHERE substr(id,-2) IN ('02','06','12');
UPDATE intensity_point SET at_epoch_millis=at_epoch_millis+172800000 WHERE substr(attack_id,-2) IN ('02','06','12');
COMMIT;
SELECT strftime('%w',start_epoch_millis/1000,'unixepoch','+5 hours') d, count(*) FROM attack GROUP BY d;
