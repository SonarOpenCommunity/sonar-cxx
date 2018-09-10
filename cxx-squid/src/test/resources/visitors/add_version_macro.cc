// Example from https://www.sonarsource.com/docs/CognitiveComplexity.pdf version 1.2 page 16
// same as add_version.cc, but the whole implementation is extracted to macro

#define IMPLEMENTATION()                                                                    \
   TransactionIndex ti = _persistit.getTransactionIndex();                                  \
   while (true) {                                                                           \
      try {                                                                                 \
         if (frst != null) {                                                                \
            if (frst.getVersion() > entry.getVersion()) {                                   \
               RollbackException();                                                         \
            }                                                                               \
            if (txn.isActive()) {                                                           \
               for (Entry e = frst;                                                         \
                    e != nullptr;                                                           \
                    e = e.getPrevious()) {                                                  \
                  long version = e.getVersion();                                            \
                  long depends = ti.wwDependency(version, txn.getTransactionStatus(), 0);   \
                  if (depends == TIMED_OUT) {                                               \
                     WWRetryException(version);                                             \
                  }                                                                         \
                  if (depends != 0                                                          \
                      && depends != ABORTED) {                                              \
                     RollbackException();                                                   \
                  }                                                                         \
               }                                                                            \
            }                                                                               \
            entry.setPrevious(frst);                                                        \
            frst = entry;                                                                   \
         }                                                                                  \
      } catch (WRetryException re) {                                                        \
         try {                                                                              \
            long depends = _persistit.getTransactionIndex()                                 \
                                     .wwDependency(re.getVersionHandle(),                   \
                                                   txn.getTransactionStatus(),              \
                                                   SharedResource.DEFAULT_MAX_WAIT_TIME);   \
            if (depends != 0                                                                \
                && depends != ABORTED) {                                                    \
               RollbackException();                                                         \
            }                                                                               \
         } catch (InterruptedException ie) {                                                \
            PersistitInterruptedException(ie);                                              \
         }                                                                                  \
      } catch (InterruptedException ie) {                                                   \
         PersistitInterruptedException(ie);                                                 \
      }                                                                                     \
   }                                                                                        \


void addVersion(Entry entry, Transaction txn)
{
   IMPLEMENTATION()                          // macro was expanded but generated code was not considered as complexity source
   if ("1" < entry.getVersion()) {           // +1
      RollbackException();
   }
}
