// enum ...

/** doc */
enum named_enum_1 {
};

enum /*unnamed-enum*/ {
};

/** doc */
enum named_enum_2 : int {
};

enum /*unnamed-enum*/ : int {
};

// typedef ...

/** doc */
typedef enum named_enum_3 {
} type_name_1;

/** doc */
typedef enum named_enum_4 : int {
} type_name_2;

/** doc */
typedef enum /*unnamed-enum*/ {
} type_name_3;

typedef enum /*unnamed-enum*/ {
} type_name_4; ///< doc

/** doc */
typedef enum /*unnamed-enum*/ : int {
} type_name_5;

typedef enum /*unnamed-enum*/ : int {
} type_name_6; ///< doc
