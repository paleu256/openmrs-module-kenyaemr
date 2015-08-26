package org.openmrs.module.kenyaemr.calculation.library.hiv;

import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyaemr.HivConstants;
import org.openmrs.module.kenyaemr.calculation.library.models.LostToFU;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Calculates the date a patient was declared lost
 */
public class DateClassifiedLTFUCalculation extends AbstractPatientCalculation {

    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {

        //find the return visit date from the last encounter
        CalculationResultMap resultMap = calculate(new LastReturnVisitDateCalculation(), cohort, context);
        //find lost to follow up patients
        CalculationResultMap lostPatients = calculate(new LostToFollowUpCalculation(), cohort, context);

        CalculationResultMap ret = new CalculationResultMap();

        for (Integer ptId : cohort) {
            LostToFU classifiedLTFU = null;
            Boolean lost = (Boolean) lostPatients.get(ptId).getValue();
            // Is patient alive and in the HIV program
            if (lost != null && lost.equals(Boolean.TRUE)) {
                SimpleResult lastScheduledReturnDateResults = (SimpleResult) resultMap.get(ptId);

                if (lastScheduledReturnDateResults != null) {
                    Date lastScheduledReturnDate = (Date) lastScheduledReturnDateResults.getValue();
                    if(lastScheduledReturnDate != null) {
                        Calendar dateClassified = Calendar.getInstance();
                        dateClassified.setTime(lastScheduledReturnDate);
                        dateClassified.add(Calendar.DATE, HivConstants.LOST_TO_FOLLOW_UP_THRESHOLD_DAYS);
                        classifiedLTFU = new LostToFU(true, dateClassified.getTime());
                    }
                }

            }
            else {
                classifiedLTFU = new LostToFU(false, null);
            }

            ret.put(ptId, new SimpleResult(classifiedLTFU, this));
        }
        return ret;
    }
}