#ifndef ZLIB_FIX_H
#define ZLIB_FIX_H

// This header is included to fix the fdopen macro conflict in zlib
// We need to undefine the problematic macro before system headers are included

#ifdef fdopen
#undef fdopen
#endif

// Define HAVE_FDOPEN to prevent zlib from redefining it
#ifndef HAVE_FDOPEN
#define HAVE_FDOPEN 1
#endif

#endif // ZLIB_FIX_H