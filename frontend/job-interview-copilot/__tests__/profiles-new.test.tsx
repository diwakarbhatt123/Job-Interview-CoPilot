import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import NewProfile from "@/pages/profiles/new";

const push = jest.fn();
const replace = jest.fn();

jest.mock("next/router", () => ({
  useRouter: () => ({
    push,
    replace,
  }),
}));

beforeEach(() => {
  push.mockReset();
  replace.mockReset();
  global.fetch = jest.fn();
});

it("requires display name", async () => {
  render(<NewProfile />);

  fireEvent.click(screen.getByRole("button", {name: /create profile/i}));

  expect(await screen.findByText(/display name is required/i)).toBeInTheDocument();
});

it("requires pasted CV in paste mode", async () => {
  render(<NewProfile />);

  fireEvent.change(screen.getByLabelText(/display name/i), {
    target: {value: "Primary"},
  });

  fireEvent.click(screen.getByRole("button", {name: /create profile/i}));

  expect(await screen.findByText(/please paste your cv/i)).toBeInTheDocument();
});

it("clears pasted CV when switching to upload", () => {
  render(<NewProfile />);

  const textarea = screen.getByLabelText(/paste your cv/i);
  fireEvent.change(textarea, {target: {value: "text"}});

  fireEvent.click(screen.getByRole("button", {name: /upload cv/i}));

  expect((textarea as HTMLTextAreaElement).value).toBe("");
});

it("redirects to login on 401", async () => {
  (global.fetch as jest.Mock).mockResolvedValue({
    ok: false,
    status: 401,
    json: async () => ({error: "Unauthorized"}),
  });

  render(<NewProfile />);

  fireEvent.change(screen.getByLabelText(/display name/i), {
    target: {value: "Primary"},
  });
  fireEvent.change(screen.getByLabelText(/paste your cv/i), {
    target: {value: "resume"},
  });

  fireEvent.click(screen.getByRole("button", {name: /create profile/i}));

  await waitFor(() => expect(push).toHaveBeenCalledWith("/login"));
});
