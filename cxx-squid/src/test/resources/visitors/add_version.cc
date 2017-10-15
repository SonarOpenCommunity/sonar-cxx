// Example from https://www.sonarsource.com/docs/CognitiveComplexity.pdf version 1.2 page 16

void addVersion(Entry entry, Transaction txn)
{
   TransactionIndex ti = _persistit.getTransactionIndex();
   while (true) {                                         // +1
      try {
         if (frst != null) {                              // +2 (nesting = 1)
            if (frst.getVersion() > entry.getVersion()) { // +3 (nesting = 2)
               RollbackException();
            }
            if (txn.isActive()) {                         // +3 (nesting = 2)
               for (Entry e = frst;
                    e != nullptr;
                    e = e.getPrevious()) {                // +4 (nesting = 3)
                  long version = e.getVersion();
                  long depends = ti.wwDependency(version, txn.getTransactionStatus(), 0);
                  if (depends == TIMED_OUT) {             // +5 (nesting = 4)
                     WWRetryException(version);
                  }
                  if (depends != 0                        // +5 (nesting = 4)
                      && depends != ABORTED) {            // +1
                     RollbackException();
                  }
               }
            }
            entry.setPrevious(frst);
            frst = entry;
         }
      } catch (WRetryException re) {                      // +2 (nesting = 1)
         try {
            long depends = _persistit.getTransactionIndex()
                                     .wwDependency(re.getVersionHandle(),
                                                   txn.getTransactionStatus(),
                                                   SharedResource.DEFAULT_MAX_WAIT_TIME);
            if (depends != 0                             // +3 (nesting = 2)
                && depends != ABORTED) {                 // +1
               RollbackException();
            }
         } catch (InterruptedException ie) {             // +3 (nesting = 2)
            PersistitInterruptedException(ie);
         }
      } catch (InterruptedException ie) {                // +2 (nesting = 1)
         PersistitInterruptedException(ie);
      }
   }
}                                                        // total complexity = 35
