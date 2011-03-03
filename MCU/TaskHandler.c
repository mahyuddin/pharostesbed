#include <hidef.h>           /* common defines and macros */
#include <mc9s12dp512.h>     /* derivative information */
#include "Taskhandler.h"
#include "Types.h"
#include "LED.h"

typedef void (*fncPtr)();

/**
 * Define the array of function pointers.
 * This points to tasks that need to be executed.
 */
//void (*_taskArray[TASK_QUEUE_SIZE])(void){0};
fncPtr _taskArray[TASK_QUEUE_SIZE];
uint16_t _taStartIndx;
uint16_t _taEndIndx;

/**
 * Calculates the next index into the task array.
 */
uint16_t nextTAIndx(uint16_t indx) {
	return (indx + 1) % TASK_QUEUE_SIZE;
}

void TaskHandler_init(void) {
	_taStartIndx = 0;
	_taEndIndx = 0;
}

/**
 * Searches the list of pending tasks and determines whether the
 * specified task is already pending.
 *
 * @param fucptr The task to search for.
 * @return TRUE if the task is already pending, FALSE otherwise.
 */
bool TaskHandler_taskIsPending(void(*funcptr)(void)) {
	uint16_t i;
	// verify that funcptr does not already exist in the queue
	for (i = _taStartIndx; i < _taEndIndx; i++) {
		if (_taskArray[i] == funcptr)
			return TRUE;
	}
	return FALSE;
}

/**
 * Adds a new task to the task queue.
 * This is called within an interrupt context.
 */
void TaskHandler_postTask(void(*funcptr)(void)) {
	if (!TaskHandler_taskIsPending(funcptr)) {
		
		// Get the next end index to make room for the new task
		uint16_t nxtEndIndx = nextTAIndx(_taEndIndx);
		
		if (nxtEndIndx != _taStartIndx) { // if there is still space in the queue
			_taskArray[_taEndIndx] = funcptr; // save the data
			_taEndIndx = nxtEndIndx; // advance the end index
		} else {
			// The task buffer overflowed!
			//LED_RED1 ^= 1;
		}
	} else {
		// task is already pending...ignore request
		//LED_YELLOW2 ^= 1;
	}
}

void TaskHandler_processNextTask(void) {
	if (_taStartIndx != _taEndIndx) { // If there are pending tasks
		void (*nxtTask)(void) = _taskArray[_taStartIndx];
		_taStartIndx = nextTAIndx(_taStartIndx); // remove the task from the queue by advancing the start pointer
		nxtTask(); // execute the task
	}
}
