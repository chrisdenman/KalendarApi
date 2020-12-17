(function iefe() {
    <<common_functions>>

    let calendarName = "<<calendarName>>";
    let eventUid = "<<eventUid>>";
    try {
        let calendar = getCalendarByName(calendarName);
        if (calendar !== undefined) {
            let event = getEventByUid(calendar, eventUid);
            if (event !== undefined) {
                return JSON.stringify(toJSONEvent(event));
            }
        }
    } catch (error) {
    }
    return "";
})()
