package com.debuglab.reloop.model;

public class Component {
    private String id;
    private String componentType;
    private String manufacturer;
    private String compatibilityFamily;
    private String interfaceType;
    private int capacity;
    private ConditionGrade conditionGrade;
    private ComponentAvailability availabilityStatus;
    private String sourceDeviceId;
    private String reservedForJobId;

    public Component() {
    }

    public Component(String id, String componentType, String manufacturer, String compatibilityFamily,
                     String interfaceType, int capacity, ConditionGrade conditionGrade,
                     ComponentAvailability availabilityStatus, String sourceDeviceId, String reservedForJobId) {
        this.id = id;
        this.componentType = componentType;
        this.manufacturer = manufacturer;
        this.compatibilityFamily = compatibilityFamily;
        this.interfaceType = interfaceType;
        this.capacity = capacity;
        this.conditionGrade = conditionGrade;
        this.availabilityStatus = availabilityStatus;
        this.sourceDeviceId = sourceDeviceId;
        this.reservedForJobId = reservedForJobId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCompatibilityFamily() {
        return compatibilityFamily;
    }

    public void setCompatibilityFamily(String compatibilityFamily) {
        this.compatibilityFamily = compatibilityFamily;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public ConditionGrade getConditionGrade() {
        return conditionGrade;
    }

    public void setConditionGrade(ConditionGrade conditionGrade) {
        this.conditionGrade = conditionGrade;
    }

    public ComponentAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(ComponentAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getSourceDeviceId() {
        return sourceDeviceId;
    }

    public void setSourceDeviceId(String sourceDeviceId) {
        this.sourceDeviceId = sourceDeviceId;
    }

    public String getReservedForJobId() {
        return reservedForJobId;
    }

    public void setReservedForJobId(String reservedForJobId) {
        this.reservedForJobId = reservedForJobId;
    }

    public boolean isAvailable() {
        return this.availabilityStatus == ComponentAvailability.AVAILABLE && this.reservedForJobId == null;
    }
}
