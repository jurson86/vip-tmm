package com.tuandai.ar.rtc.enums;


/**
 * @author Gus Jiang
 * @date 2019/3/11  15:54
 */

public enum PropagationEnum {

    /**
     * PropagationEnum required propagation.
     */
    PROPAGATION_REQUIRED(0),

    /**
     * PropagationEnum requires new propagation.
     */
    PROPAGATION_REQUIRES_NEW(1),

    /**
     * PropagationEnum mandatory propagation.
     */
    PROPAGATION_MANDATORY(2);


    private final int value;

    PropagationEnum(int value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public int getValue() {
        return this.value;
    }


}
