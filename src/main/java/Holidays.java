import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.DayOfWeek.*;
import static java.time.Month.*;

public class Holidays {
    private List<Holiday> holidays = new ArrayList<>();

    private Holidays(List<Holiday> holidayList) {
        this.holidays = holidayList;
    }

    public List<Holiday> getHolidays() {
        return Collections.unmodifiableList(holidays);

    }

    public List<LocalDate> getAllDates() {
        return getLocalDateStream()
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean isHoliday(LocalDate day) {
        return holidays
                .stream()
                .map(Holiday::getDate).anyMatch(date -> date.equals(day));
    }

    private Stream<LocalDate> getLocalDateStream() {
        return holidays
                .stream()
                .map(Holiday::getDate);
    }

    public static class HolidaysBuilder {
        private static Map<String, Function<Integer, LocalDate>> holidayFunctions = new HashMap<>();
        private final Set<Holiday> holidays = new HashSet<>();

        private int[] years = new int[]{LocalDate.now().getYear()};
        private static final String NEW_YEARS = "New Years";

        private enum WEEK_OF_MONTH {
            FIRST(1), SECOND(2), THIRD(3), FOURTH(4), FIFTH(5), LAST(-1);
            private final int number;

            WEEK_OF_MONTH(int num) {
                this.number = num;
            }
        }

        private static final BiFunction<Month, Integer, Function<Integer, LocalDate>> observableHoliday = (month, day) -> (year) -> getObserved(LocalDate.of(year, month, day));

        private static Function<Integer, LocalDate> getHolidayFunction(WEEK_OF_MONTH week, DayOfWeek day, Month month) {
            return (year) -> Year.of(year).atMonth(month).atDay(1)
                    .with(TemporalAdjusters.dayOfWeekInMonth(week.number, day));
        }

        public HolidaysBuilder forYears(int... years) {
            this.years = years;
            return this;
        }

        public HolidaysBuilder withFederalHolidays() {
            holidayFunctions = Map.of(
                    "Memorial Day", getHolidayFunction(WEEK_OF_MONTH.LAST, MONDAY, MAY),
                    "Thanksgiving", getHolidayFunction(WEEK_OF_MONTH.FOURTH, THURSDAY, NOVEMBER),
                    "Christmas", observableHoliday.apply(DECEMBER, 25),
                    "Fourth of July", observableHoliday.apply(JULY, 4),
                    NEW_YEARS, observableHoliday.apply(JANUARY, 1)
            );
            return this;
        }

        public Holidays build() {
            for (int year : this.years) {
                populateHolidaysIfEmpty(year);
            }
            List<Holiday> holidayList = new ArrayList<>(holidays);
            return new Holidays(holidayList);
        }

        private static LocalDate getObserved(LocalDate date) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            if (dayOfWeek == SATURDAY) {
                return date.plusDays(-1);
            } else if (dayOfWeek == SUNDAY) {
                return date.plusDays(1);
            }
            return date;
        }

        private void buildHolidayList(int year) {
            List<HolidayDate> _holidays = holidayFunctions.entrySet().stream()
                    .map(e -> new HolidayDate(e.getKey(), e.getValue().apply(year)))
                    .collect(Collectors.toList());

            //map needs to include next new years in case search for 12/31 of this year
            _holidays.add(new HolidayDate(NEW_YEARS, holidayFunctions.get(NEW_YEARS).apply(year + 1)));
            this.holidays.addAll(_holidays);
        }


        //Build holiday map if it doesn't exist
        private boolean doesHolidayListForYearExist(int year) {
            return holidays
                    .stream()
                    .map(Holiday::getDate)
                    .map(LocalDate::getYear)
                    .filter(current -> current == year)
                    .count() > 1;
        }

        private void populateHolidaysIfEmpty(int year) {
            if (doesHolidayListForYearExist(year)) return;
            buildHolidayList(year);

        }


    }

    private static class HolidayDate implements Holiday {
        private final String name;
        private final LocalDate date;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HolidayDate that = (HolidayDate) o;
            return date.equals(that.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date);
        }

        public HolidayDate(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public LocalDate getDate() {
            return date;
        }

    }

}
