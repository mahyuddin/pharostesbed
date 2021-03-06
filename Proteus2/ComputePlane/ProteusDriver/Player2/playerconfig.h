/* #undef HAVE_GEOS 1 */
#define HAVE_CFMAKERAW 1
#define HAVE_DIRNAME 1
/* #undef HAVE_LIBLTDL 1 */
/* #undef HAVE_PLAYERSD 1 */
#define HAVE_POLL 1
#define HAVE_POLLFD 1
#define HAVE_POLLIN 1
#define HAVE_ROUND 1
#define HAVE_RINT 1
#define HAVE_STDINT_H 1
#define HAVE_COMPRESSBOUND 1
#define INCLUDE_RTK 1
/* #undef INCLUDE_RTKGUI 1 */
#define HAVE_CLOCK_GETTIME 1
#define HAVE_XDR
#define HAVE_XDR_LONGLONG_T 1
#define HAVE_STL 1
#define HAVE_M 1
#define HAVE_GETTIMEOFDAY 1
#define HAVE_USLEEP 1
#define HAVE_NANOSLEEP 1
#define HAVE_STRUCT_TIMESPEC 1

#if defined HAVE_STDINT_H
  #include <stdint.h>
#endif
#include <sys/types.h>
#if defined WIN32
  #include <rpc/types.h>
#endif

/* Logging level to console and system log */
#define DEBUG_LEVEL NONE
