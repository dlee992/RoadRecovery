package nju.ics.Entity;

public class TollAndVehicleType {
    TollUnit tollUnit;
    VehicleType vehicleType;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TollAndVehicleType)) return false;
        TollAndVehicleType another = (TollAndVehicleType) obj;
        return tollUnit == another.tollUnit && vehicleType == another.vehicleType;
    }
}
