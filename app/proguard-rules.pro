# Migrely's R8 rules for the release build (BRD D102).
#
# Room, Hilt, Compose, Navigation, WorkManager and kotlinx.serialization ship their own consumer rules, so this file
# holds only rules for code that R8 breaks, each with a comment saying why. Add a rule only for a breakage found by
# the release smoke, and keep it as narrow as the breakage.
