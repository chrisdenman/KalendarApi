(function iefe() {
    <<common_functions>>

    let calendarName = "<<calendarName>>";
    try {
        let calendar = getCalendarByName(calendarName);
        let eventData = <<event>>;
        eventData = createDatesIfNecessary(eventData, "startDate")
        eventData = createDatesIfNecessary(eventData, "endDate")
        if (calendar !== undefined) {
            let event = calendarApp.Event(eventData);
            calendar.events.push(event);
            return event.uid();
        }
    } catch (error) {
        return String(error);
    }
    return "";
})()
