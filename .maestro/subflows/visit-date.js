// For DATE (an ISO date, when given: device scenarios move the emulator's clock away from the host's) or else today +
// OFFSET days by the host's clock, which the QA emulator follows, writes:
//   output.visitSpoken  the date as the app speaks it in en-US, for example "Friday, November 20, 2026";
//   output.visitMonths  how many months after the picker's first month that date lies (0 or more).
var DAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']
var MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October',
  'November', 'December']
var now = new Date()
var given = typeof DATE !== 'undefined' && DATE ? DATE.split('-').map(Number) : null
var d = given ? new Date(given[0], given[1] - 1, given[2])
  : new Date(now.getFullYear(), now.getMonth(), now.getDate() + Number(OFFSET))
output.visitSpoken = DAYS[d.getDay()] + ', ' + MONTHS[d.getMonth()] + ' ' + d.getDate() + ', ' + d.getFullYear()
output.visitMonths = (d.getFullYear() - now.getFullYear()) * 12 + d.getMonth() - now.getMonth()
