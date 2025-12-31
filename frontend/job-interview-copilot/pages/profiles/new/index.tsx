import Link from "next/link";
import {useRouter} from "next/router";
import {FormEvent, useRef, useState} from "react";

export default function NewProfile() {
  const router = useRouter();
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mode, setMode] = useState<"paste" | "upload">("paste");
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);
  const maxUploadBytes = 5 * 1024 * 1024;
  const pasteRef = useRef<HTMLTextAreaElement | null>(null);
  const fileRef = useRef<HTMLInputElement | null>(null);

  async function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (file) {
      if (file.type !== 'application/pdf') {
        setError('Only PDF files are supported.')
        setUploadedFile(null);
        if (fileRef.current) {
          fileRef.current.value = "";
          fileRef.current.focus();
        }
        return
      }
      if (file.size > maxUploadBytes) {
        setError('File size must be 5 MB or less.')
        setUploadedFile(null);
        if (fileRef.current) {
          fileRef.current.value = "";
          fileRef.current.focus();
        }
        return
      }
      setUploadedFile(file);
    }
  }


  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const formData = new FormData(event.currentTarget)
      const displayName = String(formData.get('displayName') || '').trim()
      const pastedCV = String(formData.get('cvPaste') || '').trim()
      const file = mode === 'upload' ? (uploadedFile ?? (formData.get('cvFile') as File | null)) : null

      if (!displayName) {
        setError('Display name is required.')
        return
      }
      if (mode === 'paste' && !pastedCV) {
        setError('Please paste your CV.')
        return
      }
      if (mode === 'upload' && (!file || file.size === 0)) {
        setError('Please upload your CV file.')
        return
      }
      if (mode === 'upload' && file && file.type !== 'application/pdf') {
        setError('Only PDF files are supported.')
        return
      }

      const response = await (async () => {
        if (mode === 'paste') {
          const body = JSON.stringify({
            'displayName': displayName,
            'pastedCV': pastedCV,
            'sourceType': "PASTED",
          })
          return fetch("/api/profile/new", {
            method: "POST",
            headers: {
              'Content-Type': 'application/json'
            },
            body: body
          })
        }

        const form = new FormData()
        form.append('displayName', displayName)
        form.append('sourceType', 'UPLOADED')
        if (file) {
          form.append('resume', file)
        }
        return fetch("/api/profile/upload", {
          method: "POST",
          body: form,
        })
      })()

      if (response.ok) {
        const res = await response.json()
        const profileId = res['id']
        await router.replace(`/profiles/${profileId}`)
        return
      }

      if (response.status === 401) {
        await router.push('/login')
        return
      }

      if (response.status === 400 || response.status === 409) {
        const payload = await response.json().catch(() => null)
        const message =
          payload?.error || payload?.message || 'Invalid input. Please check and try again.'
        setError(message)
        return
      }

      setError(`Something went wrong. Please try again.`)
    } catch (e) {
      console.error(e)
      setError('Network error. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="bg-gray-50">
      <div
        className="mx-auto flex flex-col items-center justify-center px-6 py-8 md:h-screen lg:py-0">
        <div className="w-full rounded-lg bg-white shadow sm:max-w-lg md:max-w-3xl md:mt-0 xl:p-0">
          <div className="space-y-4 p-6 sm:p-8 md:space-y-6">
            <p className="text-left text-sm font-light text-gray-500">
              <Link
                href={'/profiles'}
                className="text-center font-medium text-blue-600 hover:underline"
              >
                {'← Back to dashboard'}
              </Link>
            </p>
            <h1
              className="text-center text-3xl leading-tight font-bold tracking-normal text-gray-900 md:text-3xl">
              Create your profile
            </h1>
            <h2 className="text-center leading-tight tracking-tight text-gray-500">
              Upload or paste your CV so we can extract your skills and experience
            </h2>
            <form className="space-y-4 md:space-y-6" onSubmit={onSubmit} noValidate>
              <div>
                <label
                  htmlFor="displayName"
                  className="mb-2 block text-sm font-medium text-gray-900"
                >
                  Display Name
                </label>
                <input
                  type="text"
                  id="displayName"
                  name="displayName"
                  placeholder="John Doe Backend"
                  required
                  className="focus:ring-primary-600 focus:border-primary-600 block w-full rounded-lg border border-gray-300 bg-gray-50 p-2.5 text-gray-900"
                />
              </div>
              <div className="sm:hidden">
                <label htmlFor="tabs" className="sr-only">Choose CV mode</label>
                <select
                  id="tabs"
                  value={mode}
                  onChange={(e) => setMode(e.target.value as "paste" | "upload")}
                  className="block w-full rounded-base border border-default-medium bg-neutral-secondary-medium px-3 py-2.5 text-sm"
                >
                  <option value="paste">Paste CV</option>
                  <option value="upload">Upload CV</option>
                </select>
                <p className="mt-2 text-xs text-gray-500">
                  {mode === "paste" ? "Paste mode selected" : "Upload mode selected"}
                </p>
              </div>
              <ul
                className="relative hidden sm:flex rounded-md bg-gray-200 text-sm font-medium overflow-hidden">
                {/* Sliding background */}
                <span
                  className={`absolute inset-y-0 w-1/2 rounded-md bg-black transition-transform duration-300 ease-in-out ${
                    mode === "paste" ? "translate-x-0" : "translate-x-full"
                  }`}
                />

                {/* Paste tab */}
                <li className="relative z-10 w-1/2">
                  <button
                    type="button"
                    onClick={() => {
                      setMode("paste")
                      if (fileRef.current) fileRef.current.value = ""
                      setUploadedFile(null);
                    }}
                    className={`w-full px-4 py-2.5 transition-colors duration-300 ${
                      mode === "paste" ? "text-white" : "text-black"
                    }`}
                  >
                    Paste CV
                  </button>
                </li>

                {/* Upload tab */}
                <li className="relative z-10 w-1/2">
                  <button
                    type="button"
                    onClick={() => {
                      setMode("upload")
                      if (pasteRef.current) pasteRef.current.value = ""
                    }}
                    className={`w-full px-4 py-2.5 transition-colors duration-300 ${
                      mode === "upload" ? "text-white" : "text-black"
                    }`}
                  >
                    Upload CV
                  </button>
                </li>
              </ul>
              <div className="relative mt-4 overflow-hidden">
                <div
                  className={`flex w-[200%] transition-transform duration-300 ease-in-out ${
                    mode === "paste" ? "translate-x-0" : "-translate-x-1/2"
                  }`}
                >
                  {/* Paste CV panel */}
                  <div className="w-1/2 pr-2">
                    <label htmlFor="cvPaste" className="sr-only">
                      Paste your CV
                    </label>
                    <textarea
                      id="cvPaste"
                      name="cvPaste"
                      placeholder="Paste your CV content here"
                      rows={10}
                      ref={pasteRef}
                      className="block w-full rounded-lg border border-gray-300 bg-gray-50 p-2.5 text-gray-900 focus:border-primary-600 focus:ring-primary-600"
                    />
                  </div>

                  {/* Upload CV panel */}
                  {/* Upload CV panel */}
                  <div className="w-1/2 pl-2">
                    <div className="flex w-full flex-col gap-3">
                      {/* EMPTY STATE: big dropzone */}
                      {!uploadedFile && (
                        <label
                          htmlFor="dropzone-file"
                          className="
          flex min-h-55 w-full flex-col items-center justify-center gap-3
          rounded-xl border border-dashed border-gray-300 bg-gray-50
          px-6 py-8 text-center transition hover:bg-gray-100
        "
                        >
                          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-white shadow-sm">
                            <svg
                              className="h-6 w-6 text-gray-500"
                              xmlns="http://www.w3.org/2000/svg"
                              fill="none"
                              viewBox="0 0 24 24"
                              stroke="currentColor"
                              strokeWidth="1.8"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1M16 8l-4-4m0 0L8 8m4-4v12"
                              />
                            </svg>
                          </div>

                          <div className="space-y-1">
                            <p className="text-sm">
                              <span className="font-medium text-gray-900">Click to upload</span>{" "}
                              <span className="text-gray-500">or drag and drop</span>
                            </p>
                            <p className="text-xs text-gray-400">
                              PDF only • up to 5 MB
                            </p>
                          </div>
                        </label>
                      )}

                      {/* UPLOADED STATE: compact card */}
                      {uploadedFile && (
                        <div className="flex w-full flex-col gap-2 rounded-xl border border-green-200 bg-green-50/60 px-4 py-3">
                          <div className="flex items-start gap-3">
                            <div className="mt-0.5 flex h-6 w-6 items-center justify-center rounded-full bg-white">
                              <svg
                                className="h-4 w-4 text-green-600"
                                xmlns="http://www.w3.org/2000/svg"
                                fill="none"
                                viewBox="0 0 24 24"
                                stroke="currentColor"
                                strokeWidth="2"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  d="M5 13l4 4L19 7"
                                />
                              </svg>
                            </div>
                            <div className="flex-1">
                              <p className="truncate text-sm font-medium text-gray-900">
                                {uploadedFile.name}
                              </p>
                              <p className="text-xs text-gray-500">
                                {(uploadedFile.size / 1024).toFixed(1)} KB · Uploaded
                              </p>
                            </div>
                          </div>

                          <div className="flex flex-wrap items-center gap-3 pl-9">
                            <button
                              type="button"
                              className="text-xs font-medium text-gray-700 underline-offset-2 hover:underline"
                              onClick={() => {
                                fileRef.current?.click(); // reopen picker
                              }}
                            >
                              Change file
                            </button>
                            <button
                              type="button"
                              className="text-xs font-medium text-red-600 underline-offset-2 hover:underline"
                              onClick={() => {
                                setUploadedFile(null);
                                if (fileRef.current) {
                                  fileRef.current.value = "";
                                }
                              }}
                            >
                              Remove file
                            </button>
                          </div>
                        </div>
                      )}

                      {/* Hidden input shared by both states */}
                      <input
                        id="dropzone-file"
                        name="cvFile"
                        type="file"
                        className="hidden"
                        ref={fileRef}
                        onChange={handleFileChange}
                        accept=".pdf,application/pdf"
                      />
                    </div>
                  </div>
                </div>
              </div>
              {error && (
                <p className="text-sm font-light text-red-700" role="alert">{error}</p>
              )}
              <button
                type="submit"
                disabled={loading}
                className={`focus:ring-primary-300 flex w-full items-center justify-center gap-2 rounded-lg bg-black px-5 py-2.5 text-center text-sm font-medium text-white hover:bg-gray-800 focus:ring-4 focus:outline-none ${
                  loading ? 'cursor-not-allowed opacity-70' : ''
                }`}
              >
                {loading && (
                  <svg
                    aria-hidden="true"
                    className="h-4 w-4 animate-spin"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                      fill="none"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
                    />
                  </svg>
                )}
                {loading ? 'Creating...' : 'Create profile'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </section>
  )
}
