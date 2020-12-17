let applicationName = "Calendar";
    let app = Application.currentApplication();
    app.includeStandardAdditions = true;
    let calendarApp = Application(applicationName);

    function createDatesIfNecessary(data, propertyName) {
        if (data.hasOwnProperty(propertyName)) {
            data[propertyName] = new Date(data[propertyName]);
        }
        return data;
    }

    function getEventDateTime(eventDate) {
        return new Date(eventDate).getTime();
    }

    function getEventByUid(calendar, eventUid) {
        return calendar.events.byId(eventUid);
    }

    function getCalendarByName(calendarName) {
        let calendars = calendarApp.calendars.whose({name: calendarName})
        let result = undefined;
        if (calendars.length === 1) {
            result = calendars.at(0);
        }
        return result;
    }

    function getExcludedDatesTimes(event) {
        let excludedDates = event.excludedDates()[0];
        let excludedDatesObjects = [];
        let excludedDatesIndex = undefined;
        for (excludedDatesIndex in excludedDates) {
            if (excludedDates.hasOwnProperty(excludedDatesIndex)) {
                let excludedDate = excludedDates[excludedDatesIndex];
                excludedDatesObjects.push(getEventDateTime(excludedDate));
            }
        }
        return excludedDatesObjects;
    }

    function toJSONCalendar(calendar) {
        return {
            "name": calendar.name().toString(),
            "description": calendar.description().toString()
        };
    }

    function toStringIfNotNullElseEmptyString(value) {
        return value == null ? "" : value.toString();
    }

    function toJSONEvent(event) {
        return {
            "description": toStringIfNotNullElseEmptyString(event.description()),
            "startDate": getEventDateTime(event.startDate()),
            "endDate": getEventDateTime(event.endDate()),
            "alldayEvent": Boolean(event.alldayEvent()),
            "recurrence": toStringIfNotNullElseEmptyString(event.recurrence()),
            "sequence": Number(event.sequence()),
            "stampDate": getEventDateTime(event.stampDate()),
            "excludedDates": getExcludedDatesTimes(event),
            "status": event.status(),
            "summary": toStringIfNotNullElseEmptyString(event.summary()),
            "location": toStringIfNotNullElseEmptyString(event.location()),
            "uid": event.uid().toString(),
            "url": toStringIfNotNullElseEmptyString(event.url())
        };
    }

    function fold(object, initial, operation) {
        let accumulator = initial;
        for (let index in object) {
            if (object.hasOwnProperty(index)) {
                accumulator = operation(accumulator, object[index])
            }
        }
        return accumulator;
    }

    function toJSONCalendars(calendars) {
        return {
            list: fold(calendars, [], function (acc, curr) {
                acc.push(toJSONCalendar(curr));
                return acc;
            })
        };
    }

    function toJSONEvents(events) {
        return {
            list: fold(events, [], function (acc, curr) {
                acc.push(toJSONEvent(curr));
                return acc;
            })
        };
    }
