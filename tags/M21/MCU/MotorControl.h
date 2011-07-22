#ifndef _MOTOR_CONTROL_H
#define _MOTOR_CONTROL_H 1

#define MAX_SPEED 2000

void MotorControl_init(void);

void MotorControl_setTargetSpeed(int16_t vel);

int16_t MotorControl_getTargetSpeed(void);

bool MotorControl_getMotorThrottled(void);

int16_t MotorControl_getMotorPower(void);

int16_t MotorControl_getTargetSpeed(void);

/**
 * Enable safe mode operation.
 */
void MotorControl_enableSafeMode(void);

/**
 * Disable safe mode operation.  This results in full mode operation (robot continues to run even
 * if motor command was not recently received).
 */
void MotorControl_disableSafeMode(void);

#endif /* _MOTOR_CONTROL_H */

