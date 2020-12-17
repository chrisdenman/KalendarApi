tell application "Calendar"
  set theCalendarName to "<<calendarName>>"
  set theEventUid to "<<eventUid>>"
  try
    set theCalendar to first calendar where its name = theCalendarName
    tell theCalendar
      set theEvent to first event where its uid = theEventUid
      set theFetchedEventUid to (get uid of theEvent)
      if theEventUid is equal to theFetchedEventUid
        delete theEvent
        save
        return theFetchedEventUid
      end if
    end tell
  on error
  end try
end tell
return ""
