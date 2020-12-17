(function iefe() {
    <<common_functions>>

    try {
        let calendarList = calendarApp.calendars;
        if (calendarList.length) {
            return JSON.stringify(toJSONCalendars(calendarList));
        }
    } catch (error) {
    }
    return "";
})()
