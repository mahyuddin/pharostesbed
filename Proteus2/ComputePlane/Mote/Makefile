COMPONENT=RadioSignalMeterAppC

PFLAGS += -I .
PFLAGS += -I ledsBlinker

#########################################################
#
# Zigbee Channel Selection
# CHANNEL_11 - 2405 MHz    CHANNEL_12 - 2410 MHz    CHANNEL_13 - 2415 MHz
# CHANNEL_14 - 2420 MHz    CHANNEL_15 - 2425 MHz    CHANNEL_16 - 2430 MHz
# CHANNEL_17 - 2435 MHz    CHANNEL_18 - 2440 MHz    CHANNEL_19 - 2445 MHz
# CHANNEL_20 - 2450 MHz    CHANNEL_21 - 2455 MHz    CHANNEL_22 - 2460 MHz
# CHANNEL_23 - 2465 MHz    CHANNEL_24 - 2470 MHz    CHANNEL_25 - 2475 MHz
# CHANNEL_26 - 2480 MHz
#
# 15, 20, 25 & 26 seem to be non-overlapping with 802.11
#########################################################
#CFLAGS +=-DCC2420_DEF_CHANNEL=11
#CFLAGS +=-DCC2420_DEF_CHANNEL=12
#CFLAGS +=-DCC2420_DEF_CHANNEL=13
#CFLAGS +=-DCC2420_DEF_CHANNEL=14
#CFLAGS +=-DCC2420_DEF_CHANNEL=15
#CFLAGS +=-DCC2420_DEF_CHANNEL=16
#CFLAGS +=-DCC2420_DEF_CHANNEL=17
#CFLAGS +=-DCC2420_DEF_CHANNEL=18
#CFLAGS +=-DCC2420_DEF_CHANNEL=19
#CFLAGS +=-DCC2420_DEF_CHANNEL=20
#CFLAGS +=-DCC2420_DEF_CHANNEL=21
#CFLAGS +=-DCC2420_DEF_CHANNEL=22
#CFLAGS +=-DCC2420_DEF_CHANNEL=23
#CFLAGS +=-DCC2420_DEF_CHANNEL=24
#CFLAGS +=-DCC2420_DEF_CHANNEL=25
CFLAGS +=-DCC2420_DEF_CHANNEL=26

#CFLAGS +=-DCC2420_TXPOWER=TXPOWER_MAX
#CFLAGS +=-DCC2420_TXPOWER=TXPOWER_M0DBM
#CFLAGS +=-DCC2420_TXPOWER=TXPOWER_M3DBM
#CFLAGS +=-DCC2420_TXPOWER=TXPOWER_M5DBM
#CFLAGS +=-DCC2420_TXPOWER=TXPOWER_M10DBM
#CFLAGS +=-DCC2420_TXPOWER=TXPOWER_M15DBM
#CFLAGS +=-DCC2420_TXPOWER=TXPOWER_M25DBM
#CFLAGS +=-DCC2420_TXPOWER=TXPOWER_MIN

CFLAGS += -DCC2420_DEF_RFPOWER=31

include $(MAKERULES)

