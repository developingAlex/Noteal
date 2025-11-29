# Noteal
Android app for taking simple notes.

## Inspiration / Motivation
Android doesn't automatically come with a note taking app so I decided to 
try my hand at making my own.

## How to use
### Creating a new note
1. Tap the top right 'kebab' menu to access the option to create a new note.
1. When the new note loads its title and contents will be blank, type 
   something in both to have a valid note to save.

### Saving a note or changes to a note
**(A note will not save without a title and some content)**
There are two ways to save a note:
- Tap the top right 'kebab' menu to access the option to Save
- Use your phones Back navigation button to return to the list of notes, the
  changes will be automatically saved.

### Deleting a note
Deleting a note can be done from the main list of notes view
1. Long press on the note you want to delete.
1. Select yes to the confirmation message.

### Exporting notes
1. Tap on the note you want to export so that you're viewing its contents
1. Tap on the top right 'kebab' menu.
1. Select the option to export the note.
1. A notification will popup showing you the path to your note, a note will
   be available from the *noteal* directory in your SDCards root directory.
  - If you had a title of *shopping* for a note you exported, then you
    should be able to find it at /noteal/noteal-shopping.txt

## Issues to resolve

- when you want to back up all your notes you have to enter each one and tap
  export, better if there were an export all option
- the app allows multiple notes with the same title but this is confusing it
  should prevent that somehow.
- when you accumulate many notes, newer ones are added to the end of the 
  list, it would be better if the list were always sorted by most recently
  modified
- it would be better to allow the user to choose what background and text
  colour they want, or at least switch between dark and light themes
- it's not clear where the exported notes go and in modern Android you
  cannot access that location from the normal files app on the phone you
  have to hook it up to a computer to browse that location.
- original implementation used a crude file storage approach as bytes rather
  than text, it might be better to keep it as text so that even the raw
  files are human readable
- there is no undo option, if you make a mistake the only option to cancel
  is to swipe away the whole app without pressing back to get back to the list,
  it would be better if it could track changes and allow you to undo and
  redo edits.
- there is a back arrow in the top left of the screen when editing a note
  that serves to take you back to the list, but this does not seem to
  trigger save like the phones native back button, at least when testing
  with a virtual device in android studio.
- When the contents of a note become very long, and a cursor is active in
  the content with the onscreen keyboard visible, sometimes it is
  impossible to scroll by swiping to see the very bottom of the content
  because its hidden behind the keyboard. A workaround is to 'drag' the
  cursor (in a similar manner to when highlighting text to copy) and drag it
  all the way to the bottom of the file.
- When the contents of a note become more than a screen's worth, attempts to
  copy parts of the text by long pressing to highlight can sometimes result
  in the on screen keyboard sliding up from the bottom, which inadvertantly
  pushes the top bar of the view being pushed up and out of the view and as
  such you lose the access to the buttons of copy, cut and paste.
