## ADDED Requirements

### Requirement: A setting that is not saved stays as stored
When a setting or mark cannot be written to the phone, Migrely SHALL show "Couldn't save that. Try again." where the
screen shows its messages, SHALL show the value that is stored, and SHALL never save the failed change later as part of
another write (BRD D178, D197, EC-STO-13).

#### Scenario: Storage full when turning off Track periods
- **WHEN** the phone's storage is full and the user turns off "Track periods"
- **THEN** "Couldn't save that. Try again." shows and the switch shows on, as stored, also after a restart

#### Scenario: A later write succeeds
- **WHEN** a switch change was not saved, space is freed, and another setting is then saved
- **THEN** the file holds the other setting's new value and the earlier stored value of the switch, not the change that
  failed
