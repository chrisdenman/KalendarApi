(function iefe() {
    <<common_functions>>

    let calendarName = "<<calendarName>>";
    try {
        let calendar = getCalendarByName(calendarName);
        if (calendar !== undefined) {
            return JSON.stringify(toJSONCalendar(calendar));
        }
    } catch (error) {
    }
    return "";
})()
